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
import { _root } from './BinderRoot.js';
import {
  _createEmptyItemValue,
  _key,
  _parent,
  _validators,
  AbstractModel,
  type ArrayItemModel,
  ArrayModel,
  ObjectModel,
  type Value,
} from './Models.js';
import type { Validator, ValueError } from './Validation.js';
import { ValidityStateValidator } from './Validators.js';
import { _validity } from './Validity.js';

export const _initializeValue = Symbol();
export const _updateValidation = Symbol();
export const _update = Symbol();
export const _setErrorsWithDescendants = Symbol();
export const _clearValidation = Symbol();

const nodes = new WeakMap<AbstractModel, BinderNode>();

export function getBinderNode<M extends AbstractModel>(model: M): BinderNode<M> {
  let node = nodes.get(model);

  if (!node) {
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    node = new BinderNode(model);
    nodes.set(model, node);
  }

  return node as BinderNode<M>;
}

function getErrorPropertyName(valueError: ValueError<any>): string {
  return typeof valueError.property === 'string' ? valueError.property : getBinderNode(valueError.property).name;
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

// eslint-disable-next-line @typescript-eslint/no-use-before-define
declare class ArrayItemBinderNode<M extends AbstractModel> extends BinderNode<M> {
  // @ts-expect-error: re-defining the parent getter.
  declare parent: BinderNode<ArrayModel<M>>;
}

const defaultArrayItemCache = new WeakMap<BinderNode, unknown>();

/**
 * The BinderNode\<T, M\> class provides the form binding related APIs
 * with respect to a particular model instance.
 *
 * Structurally, model instances form a tree, in which the object
 * and array models have child nodes of field and array item model
 * instances.
 */
export class BinderNode<M extends AbstractModel = AbstractModel> extends EventTarget {
  declare readonly ['constructor']: typeof BinderNode;
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
      this[_initializeValue]();
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
    if (this.#isArrayItem()) {
      let value = defaultArrayItemCache.get(this.parent);

      if (!value) {
        value = this.model.constructor.createEmptyValue();
        defaultArrayItemCache.set(this.parent, value);
      }

      return value as Value<M>;
    }

    const key = this.model[_key];
    return (this.parent!.defaultValue as { readonly [key in typeof key]: Value<M> })[key];
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
    let name = String(model[_key]);

    while (model[_parent] instanceof AbstractModel && model[_parent][_key] !== _root) {
      model = model[_parent];
      name = `${String(model[_key])}.${name}`;
    }

    return name;
  }

  /*
  let model = this.model as AbstractModel<any>;
    const strings = [];
    while (model[_parent] instanceof AbstractModel) {
      strings.unshift(String(model[_key]));
      model = model[_parent];
    }
    return strings.join('.');
   */

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
      this.parent[_initializeValue](true);
      ({ value } = this.parent);
    }

    const key = this.model[_key];

    // The value of parent in unknown, so we need to cast it.
    type ParentValue = { readonly [K in typeof key]: Value<M> };
    return (value as ParentValue)[key];
  }

  set value(value: Value<M> | undefined) {
    this.#setValueState(value);
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
      (this as BinderNode<ArrayModel>).value = [...(this.value ?? []), itemValueOrEmptyValue];
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
      (this as BinderNode<ArrayModel>).value = [itemValueOrEmptyValue, ...(this.value ?? [])];
    } else {
      throw new NotArrayModelError();
    }
  }

  removeSelf(): void {
    if (this.#isArrayItem()) {
      this.parent.value = (this.parent.value ?? []).filter((_, i) => i !== this.model[_key]) as Value<ArrayModel<M>>;
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
    const errors = (
      await Promise.all([...this.#requestValidationOfDescendants(), ...this.#requestValidationWithAncestors()])
    ).flat();
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

  protected [_initializeValue](requiredByChildNode = false): void {
    // First, make sure parents have value initialized
    if (
      this.parent &&
      (this.parent.value === undefined || (this.parent.defaultValue as Value<M> | undefined) === undefined)
    ) {
      this.parent[_initializeValue](true);
    }

    const key = this.model[_key];
    let value: Value<M> | undefined = this.parent
      ? (this.parent.value as { readonly [key in typeof key]: Value<M> })[this.model[_key]]
      : undefined;

    if (value === undefined) {
      // Initialize value if a child node is accessed or for the root-level node
      if (requiredByChildNode || !this.parent) {
        value = this.model.constructor.createEmptyValue() as Value<M>;
        this.#setValueState(value, this.defaultValue === undefined);
      } else if (
        this.parent.model instanceof ObjectModel &&
        !(key in ((this.parent.value || {}) as { readonly [key in typeof key]?: Value<M> }))
      ) {
        this.#setValueState(undefined, this.defaultValue === undefined);
      }
    }
  }

  *#getChildBinderNodes(): Generator<BinderNode> {
    if (this.#isObject()) {
      // We need to skip all non-initialised optional fields here in order to
      // prevent infinite recursion for circular references in the model.
      // Here we rely on presence of keys in `defaultValue` to detect all
      // initialised fields. The keys in `defaultValue` are defined for all
      // non-optional fields plus those optional fields whose values were set
      // from initial `binder.read()` or `binder.clear()` or by using a
      // binder node (e.g., form binding) for a nested field.
      if (this.defaultValue !== undefined) {
        for (const [, getter] of ObjectModel.getOwnAndParentGetters(this.model)) {
          yield getBinderNode(getter.call(this.model));
        }
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

  #requestValidationOfDescendants(): ReadonlyArray<Promise<readonly ValueError[]>> {
    return [...this.#getChildBinderNodes()].reduce<ReadonlyArray<Promise<readonly ValueError[]>>>(
      (promises, childBinderNode) => [
        ...promises,
        ...childBinderNode.#runOwnValidators(),
        ...childBinderNode.#requestValidationOfDescendants(),
      ],
      [],
    );
  }

  #requestValidationWithAncestors(): ReadonlyArray<Promise<readonly ValueError[]>> {
    return [...this.#runOwnValidators(), ...(this.parent ? this.parent.#requestValidationWithAncestors() : [])];
  }

  #runOwnValidators(): ReadonlyArray<Promise<readonly ValueError[]>> {
    if (this[_validity] && !this[_validity].valid) {
      // The element's internal validation reported invalid state.

      if (this[_validity].badInput) {
        // Bad input means the `value` cannot be used and even meaningfully
        // validated with the validators in the binder, because it cannot be
        // parsed, for example, if a date is entered with incorrect format.
        //
        // Skip running the validators, and instead assume the only error
        // from the validity state.
        return [this.binder.requestValidation(this.model, this.#validityStateValidator)];
      }
      // Validate the value, but also raise the error from the validity state.
      return [...this.#validators, this.#validityStateValidator].map(async (validator) =>
        this.binder.requestValidation(this.model, validator),
      );
    }

    return this.#validators.map(async (validator) => this.binder.requestValidation(this.model, validator));
  }

  #setValueState(value: Value<M> | undefined, keepPristine = false): void {
    const { parent } = this;
    const key = this.model[_key];

    if (parent) {
      if (parent.#isObject()) {
        // Value contained in object - replace object in parent
        parent.#setValueState({ ...parent.value, [key]: value }, keepPristine);

        return;
      }
    }

    if (value === undefined) {
      throw new TypeError('Unexpected undefined value');
    }

    if (this.#isArrayItem()) {
      // Value contained in array - replace array in parent
      const array = (this.parent.value ?? []).slice();
      array[key as number] = value;
      this.parent.#setValueState(array, keepPristine);
    } else {
      // Value contained elsewhere, probably binder - use value property setter
      const binder = this.model[_parent] as BinderRoot;

      if (keepPristine && !binder.dirty) {
        binder.defaultValue = value;
      }
      binder.value = value!;
    }
  }
}
