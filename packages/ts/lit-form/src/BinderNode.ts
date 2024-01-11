/* eslint-disable @typescript-eslint/prefer-nullish-coalescing */
/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
// TODO: Fix dependency cycle

import type { BinderRoot } from './BinderRoot.js';
import {
  _createEmptyItemValue,
  _key,
  _parent,
  _validators,
  AbstractModel,
  type ArrayItemModel,
  ArrayModel,
  getObjectModelOwnAndParentGetters,
  ObjectModel,
  type Value,
} from './Models.js';
import type { ClassStaticProperties } from './types.js';
import type { Validator, ValueError } from './Validation.js';
import { ValidityStateValidator } from './Validators.js';
import { _validity } from './Validity.js';

export const _updateValidation = Symbol('updateValidation');
export const _update = Symbol('update');
export const _setErrorsWithDescendants = Symbol('setErrorsWithDescendants');
export const _clearValidation = Symbol('clearValidation');

const nodes = new WeakMap<AbstractModel, BinderNode>();

export function getBinderNode<M extends AbstractModel>(model: M): BinderNode<M> {
  let node = nodes.get(model);

  if (!node) {
    node = new BinderNode(model);
    nodes.set(model, node);
  }

  return node as BinderNode<M>;
}

function getErrorPropertyName(valueError: ValueError): string {
  return typeof valueError.property === 'string' ? valueError.property : getBinderNode(valueError.property).name;
}

function updateObjectOrArrayKey<M extends AbstractModel>(
  model: M,
  value: Value<M>,
  key: keyof any,
  keyValue: unknown,
): Value<M> {
  if (model instanceof ObjectModel) {
    // Value contained in object - replace object in parent
    return {
      ...(value as Record<never, never> & Value<M>),
      [key]: keyValue,
    };
  }

  if (keyValue === undefined) {
    throw new TypeError('Unexpected undefined value');
  }

  if (model instanceof ArrayModel) {
    // Value contained in array - replace array in parent
    const array = (value as unknown[]).slice();
    array[key as number] = keyValue;
    return array as Value<M>;
  }

  throw new TypeError(`Unknown model type ${(model as AbstractModel).constructor.name}`);
}

export const CHANGED = new Event('binder-node-changed');

class NotArrayModelError extends Error {
  constructor() {
    super('The model does not represent array');
  }
}

class NotArrayItemModelError extends Error {
  constructor() {
    super('The model does not represent array item');
  }
}

declare class ArrayItemBinderNode<M extends AbstractModel> extends BinderNode<M> {
  // @ts-expect-error: re-defining the parent getter.
  declare readonly parent: BinderNode<ArrayModel<M>>;
}

const defaultArrayItemCache = new WeakMap<BinderNode, unknown>();

/**
 * The BinderNode\<M\> class provides the form binding related APIs
 * with respect to a particular model instance.
 *
 * Structurally, model instances form a tree, in which the object
 * and array models have child nodes of field and array item model
 * instances.
 */
export class BinderNode<M extends AbstractModel = AbstractModel> extends EventTarget {
  declare readonly ['constructor']: ClassStaticProperties<typeof BinderNode<M>>;
  readonly model: M;
  /**
   * The validity state read from the bound element, if any. Represents the
   * HTML element internal validation.
   *
   * For elements with `validity.valid === false`, the value in the
   * bound element is considered as invalid.
   */
  [_validity]?: ValidityState;
  #ownErrors?: ReadonlyArray<ValueError<Value<M>>>;
  #validators: ReadonlyArray<Validator<Value<M>>>;
  readonly #validityStateValidator: ValidityStateValidator<Value<M>>;
  #visited = false;

  constructor(model: M) {
    super();
    this.model = model;
    nodes.set(model, this);
    this.#validityStateValidator = new ValidityStateValidator<Value<M>>();
    this.#validators = model[_validators];

    // Workaround for children initialization with private props
    if (this.constructor === BinderNode) {
      this.initializeValue();
    }
  }

  /**
   * The binder for the top-level model.
   */
  get binder(): BinderRoot {
    const binder = this.parent?.binder;

    if (!binder) {
      throw new TypeError('BinderNode is detached');
    }

    return binder;
  }

  /**
   * The default value related to the model
   */
  get defaultValue(): Value<M> | undefined {
    const key = this.model[_key];
    const parentDefaultValue = this.parent!.defaultValue as { readonly [key in typeof key]?: Value<M> };

    if (this.#isArrayItem() && !(key in parentDefaultValue)) {
      if (defaultArrayItemCache.has(this.parent)) {
        return defaultArrayItemCache.get(this.parent) as Value<M>;
      }

      const value = this.model.constructor.createEmptyValue();
      defaultArrayItemCache.set(this.parent, value);
      return value as Value<M>;
    }

    return parentDefaultValue[key];
  }

  /**
   * True if the current value is different from the defaultValue.
   */
  get dirty(): boolean {
    return this.value !== this.defaultValue;
  }

  /**
   * The combined array of all errors for this node’s model and all its nested
   * models
   */
  get errors(): readonly ValueError[] {
    return [...Array.from(this.#getChildBinderNodes(), (node) => node.errors).flat(), ...this.ownErrors];
  }

  /**
   * Indicates if there is any error for the node's model.
   */
  get invalid(): boolean {
    return this.errors.length > 0;
  }

  /**
   * The name generated from the model structure, used to set the name
   * attribute on the field components.
   */
  get name(): string {
    let { model }: { model: AbstractModel } = this;
    let name = '';

    while (model[_parent] instanceof AbstractModel) {
      name = `${String(model[_key])}${name ? `.${name}` : ''}`;
      model = model[_parent];
    }

    return name;
  }

  /**
   * The array of validation errors directly related with the model.
   */
  get ownErrors(): ReadonlyArray<ValueError<Value<M>>> {
    return this.#ownErrors ? this.#ownErrors : [];
  }

  /**
   * The parent node, if this binder node corresponds to a nested model,
   * otherwise undefined for the top-level binder.
   */
  get parent(): BinderNode | undefined {
    const modelParent = this.model[_parent];
    return modelParent instanceof AbstractModel ? getBinderNode(modelParent) : undefined;
  }

  /**
   * True if the value is required to be non-empty.
   */
  get required(): boolean {
    return this.#validators.some((validator) => validator.impliesRequired);
  }

  /**
   * The array of validators for the model. The default value is defined in the
   * model.
   */
  get validators(): ReadonlyArray<Validator<Value<M>>> {
    return this.#validators;
  }

  set validators(validators: ReadonlyArray<Validator<Value<M>>>) {
    this.#validators = validators;
    this.dispatchEvent(CHANGED);
  }

  /**
   * The current value related to the model
   */
  get value(): Value<M> | undefined {
    if (!this.parent) {
      return undefined;
    }

    let { value } = this.parent;

    if (value === undefined) {
      this.parent.initializeValue(true);
      ({ value } = this.parent);
    }

    const key = this.model[_key];

    // The value of parent in unknown, so we need to cast it.
    type ParentValue = { readonly [K in typeof key]: Value<M> };
    return (value as ParentValue)[key];
  }

  set value(value: Value<M> | undefined) {
    this.initializeValue();
    this.#setValueState(value, undefined);
  }

  /**
   * True if the bound field was ever focused and blurred by the user.
   */
  get visited(): boolean {
    return this.#visited;
  }

  set visited(v: boolean) {
    if (this.#visited !== v) {
      this.#visited = v;
      this[_updateValidation]().catch(() => {});
      this.dispatchEvent(CHANGED);
    }
  }

  /**
   * A helper method to add a validator
   *
   * @param validator - a validator
   */
  addValidator(validator: Validator<Value<M>>): void {
    this.validators = [...this.#validators, validator];
    this.dispatchEvent(CHANGED);
  }

  /**
   * Append an item to the array value.
   *
   * Requires the context model to be an array reference.
   *
   * @param item - optional new item value, an empty item is
   * appended if the argument is omitted
   */
  appendItem(item?: Value<ArrayItemModel<M>>): void {
    if (this.#isArray()) {
      const itemValueOrEmptyValue = item ?? this.model[_createEmptyItemValue]();
      const newValue = [...(this.value ?? []), itemValueOrEmptyValue];
      const newDefaultValue = [...(this.defaultValue ?? []), itemValueOrEmptyValue];
      this.#setValueState(newValue, newDefaultValue);
    } else {
      throw new NotArrayModelError();
    }
  }

  /**
   * Returns a binder node for the nested model instance.
   *
   * @param model - The nested model instance
   */
  for<N extends AbstractModel>(model: N): BinderNode<N> {
    const binderNode = getBinderNode(model);
    if (binderNode.binder !== this.binder) {
      throw new Error('Unknown binder');
    }

    return binderNode;
  }

  prependItem(item?: Value<ArrayItemModel<M>>): void {
    if (this.#isArray()) {
      const itemValueOrEmptyValue = item ?? this.model[_createEmptyItemValue]();
      const newValue = [itemValueOrEmptyValue, ...(this.value ?? [])];
      const newDefaultValue = [itemValueOrEmptyValue, ...(this.defaultValue ?? [])];
      this.#setValueState(newValue, newDefaultValue);
    } else {
      throw new NotArrayModelError();
    }
  }

  removeSelf(): void {
    if (this.#isArrayItem()) {
      const newValue = (this.parent.value ?? []).filter((_, i) => i !== this.model[_key]);
      const newDefaultValue = (this.parent.defaultValue ?? []).filter((_, i) => i !== this.model[_key]);
      this.parent.#setValueState(newValue, newDefaultValue);
    } else {
      throw new NotArrayItemModelError();
    }
  }

  /**
   * Runs all validation callbacks potentially affecting this
   * or any nested model. Returns the combined array of all
   * errors as in the errors property.
   */
  async validate(): Promise<readonly ValueError[]> {
    const errors = await Promise.all([
      ...this.#requestValidationOfDescendants(),
      ...this.#requestValidationWithAncestors(),
    ]).then((arr) => arr.flat());
    this[_setErrorsWithDescendants](errors.length ? errors : undefined);
    this[_update]();
    return errors;
  }

  protected [_clearValidation](): boolean {
    if (this.#visited) {
      this.#visited = false;
      this.dispatchEvent(CHANGED);
    }
    let needsUpdate = false;
    if (this.#ownErrors) {
      this.#ownErrors = undefined;
      needsUpdate = true;
      this.dispatchEvent(CHANGED);
    }
    if ([...this.#getChildBinderNodes()].filter((childBinderNode) => childBinderNode[_clearValidation]()).length > 0) {
      needsUpdate = true;
    }
    return needsUpdate;
  }

  protected [_setErrorsWithDescendants](errors?: readonly ValueError[]): void {
    const { name } = this;
    const ownErrors = errors
      ? (errors.filter((valueError) => getErrorPropertyName(valueError) === name) as ReadonlyArray<
          ValueError<Value<M>>
        >)
      : undefined;
    const relatedErrors = errors
      ? errors.filter((valueError) => getErrorPropertyName(valueError).startsWith(name))
      : undefined;
    this.#ownErrors = ownErrors;
    for (const childBinderNode of this.#getChildBinderNodes()) {
      childBinderNode[_setErrorsWithDescendants](relatedErrors);
    }
    this.dispatchEvent(CHANGED);
  }

  protected [_update](_?: Value<M>): void {
    if (this.parent) {
      this.parent[_update]();
    }
  }

  protected async [_updateValidation](): Promise<void> {
    if (this.#visited) {
      await this.validate();
    } else if (this.dirty || this.invalid) {
      await Promise.all(
        [...this.#getChildBinderNodes()].map(async (childBinderNode) => childBinderNode[_updateValidation]()),
      );
    }
  }

  *#getChildBinderNodes(): Generator<BinderNode, void, void> {
    if (this.value === undefined || this.defaultValue === undefined) {
      // Undefined value cannot have child properties and items.
      return;
    }

    if (this.#isObject()) {
      for (const [, getter] of getObjectModelOwnAndParentGetters(this.model)) {
        const childModel = getter.call(this.model);
        // We need to skip all non-initialised optional fields here in order
        // to prevent infinite recursion for circular references in the model.
        // Here we rely on presence of keys in `defaultValue` to detect all
        // initialised fields. The keys in `defaultValue` are defined for all
        // non-optional fields plus those optional fields whose values were
        // set from initial `binder.read()` or `binder.clear()` or by using a
        // binder node (e.g., form binding) for a nested field.
        if (childModel[_key] in (this.defaultValue as Record<never, never>)) {
          yield getBinderNode(childModel);
        }
      }
    } else if (this.#isArray()) {
      for (const childBinderNode of this.model) {
        yield childBinderNode;
      }
    }
  }

  #isArray(): this is BinderNode<ArrayModel> {
    return this.model instanceof ArrayModel;
  }

  #isArrayItem(): this is ArrayItemBinderNode<M> {
    return this.model[_parent] instanceof ArrayModel;
  }

  #isObject(): this is BinderNode<ObjectModel> {
    return this.model instanceof ObjectModel;
  }

  *#requestValidationOfDescendants(): Generator<Promise<readonly ValueError[]>, void, void> {
    for (const node of this.#getChildBinderNodes()) {
      yield* node.#runOwnValidators();
      yield* node.#requestValidationOfDescendants();
    }
  }

  *#requestValidationWithAncestors(): Generator<Promise<readonly ValueError[]>, void, void> {
    yield* this.#runOwnValidators();

    if (this.parent) {
      yield* this.parent.#requestValidationWithAncestors();
    }
  }

  *#runOwnValidators(): Generator<Promise<readonly ValueError[]>, void, void> {
    const hasInvalidState = this[_validity] && !this[_validity].valid;
    const hasBadInput = !!this[_validity]?.badInput;

    // eslint-disable-next-line @typescript-eslint/prefer-nullish-coalescing
    if ((hasInvalidState && !hasBadInput) || !hasInvalidState) {
      for (const validator of this.#validators) {
        yield this.binder.requestValidation(this.model, validator);
      }
    }

    if (hasInvalidState) {
      yield this.binder.requestValidation(this.model, this.#validityStateValidator);
    }
  }

  initializeValue(forceInitialize = false): void {
    // First, make sure parents have value initialized
    if (
      this.parent &&
      (this.parent.value === undefined || (this.parent.defaultValue as Value<M> | undefined) === undefined)
    ) {
      this.parent.initializeValue(true);
    }

    const key = this.model[_key];
    let value: Value<M> | undefined = this.parent
      ? (this.parent.value as { [key in typeof key]: Value<M> })[this.model[_key]]
      : undefined;

    const defaultValue: Value<M> | undefined = this.parent
      ? (this.parent.defaultValue as { readonly [key in typeof key]: Value<M> })[this.model[_key]]
      : undefined;

    if (value === undefined) {
      // Initialize value if this is the root level node, or it is enforced
      if (forceInitialize || !this.parent) {
        value = this.model.constructor.createEmptyValue() as Value<M>;
        this.#setValueState(value, defaultValue === undefined ? value : defaultValue);
      } else if (
        this.parent.model instanceof ObjectModel &&
        !(key in ((this.parent.value || {}) as { [key in typeof key]?: Value<M> }))
      ) {
        this.#setValueState(undefined, defaultValue === undefined ? value : defaultValue);
      }
    }
  }

  #setValueState(value: Value<M> | undefined, defaultValue: Value<M> | undefined): void {
    const { parent } = this;
    if (parent) {
      const key = this.model[_key];
      const parentValue = updateObjectOrArrayKey(parent.model, parent.value, key, value);
      const keepPristine = value === defaultValue && parent.value === parent.defaultValue;
      if (keepPristine) {
        // Keep value and defaultValue equal, so that `dirty` stays false
        parent.#setValueState(parentValue, parentValue);
      } else if (defaultValue !== undefined) {
        // Update value and defaultValue at the same time with different content
        const parentDefaultValue = updateObjectOrArrayKey(parent.model, parent.defaultValue, key, defaultValue);
        parent.#setValueState(parentValue, parentDefaultValue);
      } else {
        parent.#setValueState(parentValue, undefined);
      }
    } else {
      // Root level model - update the binder root
      const binder = this as unknown as BinderRoot<M>;
      if (defaultValue !== undefined) {
        binder.defaultValue = defaultValue;
      }
      binder.value = value!;
    }
  }
}
