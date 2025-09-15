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
import m, {
  $defaultValue,
  $key,
  $optional,
  $owner,
  NumberModel,
  $constraints,
  type Constraint,
  Null,
  NotNull,
  AssertTrue,
  AssertFalse,
  Min,
  Max,
  DecimalMin,
  Negative,
  NegativeOrZero,
  Positive,
  PositiveOrZero,
  Size,
  Digits,
  Past,
  // PastOrPresent, // not supported
  Future,
  // FutureOrPresent, // not supported
  Pattern,
  NotEmpty,
  NotBlank,
  Email,
  type NonAttributedConstraint,
  Model,
} from '@vaadin/hilla-models';
import type { Constructor, EmptyObject } from 'type-fest';
import type { BinderRoot } from './BinderRoot.js';
import {
  _createEmptyItemValue,
  _validators,
  AbstractModel,
  type ArrayItemModel,
  ArrayModel,
  getObjectModelOwnAndParentGetters,
  ObjectModel,
  type Value,
} from './Models.js';
import type { ProvisionalModel } from './ProvisionalModel.js';
import type { ClassStaticProperties } from './types.js';
import type { Validator, ValueError } from './Validation.js';
import * as Validators from './Validators.js';
import { _validity } from './Validity.js';

export const _updateValidation = Symbol('updateValidation');
export const _update = Symbol('update');
export const _setErrorsWithDescendants = Symbol('setErrorsWithDescendants');
export const _clearValidation = Symbol('clearValidation');

const nodes = new WeakMap<ProvisionalModel, BinderNode>();

export function getBinderNode<M extends ProvisionalModel>(model: M): BinderNode<M> {
  let node = nodes.get(model);

  if (!node) {
    node = new BinderNode(model);
    nodes.set(model, node);
  }

  return node as BinderNode<M>;
}

function getConstraintValidator<T>(constraint: Constraint<T>): Validator<T> {
  const constraintTypes = [
    Null,
    NotNull,
    AssertTrue,
    AssertFalse,
    Min,
    Max,
    DecimalMin,
    Negative,
    NegativeOrZero,
    Positive,
    PositiveOrZero,
    Size,
    Digits,
    Past,
    // PastOrPresent, // not supported
    Future,
    // FutureOrPresent, // not supported
    Pattern,
    NotEmpty,
    NotBlank,
    Email,
  ];
  for (const constraintType of constraintTypes) {
    if (m.isConstraint(constraint, constraintType as NonAttributedConstraint)) {
      // eslint-disable-next-line import/namespace
      const Validator = Validators[constraintType.name] as Constructor<
        Validator<T>,
        EmptyObject extends typeof constraint.attributes
          ? [typeof constraint.attributes | undefined]
          : [typeof constraint.attributes]
      >;
      return new Validator(constraint.attributes);
    }
  }
  throw new Error(`Unsupported constraint: ${constraint.name}`);
}

function getModelValidators<M extends ProvisionalModel>(model: M): ReadonlyArray<Validator<Value<M>>> {
  if (model instanceof AbstractModel) {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return model[_validators];
  }

  const validators: Array<Validator<Value<M>>> =
    model instanceof NumberModel ? [new Validators.IsNumber(model[$optional]) as Validator<Value<M>>] : [];

  if (m.hasConstraints(model)) {
    for (const constraint of model[$constraints]) {
      const validator = getConstraintValidator<Value<M>>(constraint);
      validators.push(validator);
    }
  }

  return validators;
}

function createEmptyValue<M extends ProvisionalModel>(model: M) {
  if (model instanceof AbstractModel) {
    return model.constructor.createEmptyValue();
  }

  return model[$defaultValue];
}

function getErrorPropertyName(valueError: ValueError): string {
  return typeof valueError.property === 'string' ? valueError.property : getBinderNode(valueError.property).name;
}

function updateObjectOrArrayKey<M extends ProvisionalModel>(
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

  throw new TypeError(`Unknown model type ${model.constructor.name}`);
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

declare class ArrayItemBinderNode<M extends ProvisionalModel> extends BinderNode<M> {
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
export class BinderNode<M extends ProvisionalModel = ProvisionalModel> extends EventTarget {
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
  readonly #validityStateValidator: Validators.ValidityStateValidator<Value<M>>;
  #visited = false;

  constructor(model: M) {
    super();
    this.model = model;
    nodes.set(model, this);
    this.#validityStateValidator = new Validators.ValidityStateValidator<Value<M>>();
    this.#validators = getModelValidators(model);

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
    const key = this.model[$key];
    const parentDefaultValue = this.parent!.defaultValue as Readonly<Partial<Record<typeof key, Value<M>>>>;

    if (this.#isArrayItem() && !(key in parentDefaultValue)) {
      if (defaultArrayItemCache.has(this.parent)) {
        return defaultArrayItemCache.get(this.parent) as Value<M>;
      }

      const value = createEmptyValue(this.model);
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
    let { model }: { model: ProvisionalModel } = this;
    let name = '';

    while (model[$owner] instanceof AbstractModel) {
      name = `${String(model[$key])}${name ? `.${name}` : ''}`;
      model = model[$owner];
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
    const modelParent = this.model[$owner];
    return modelParent instanceof AbstractModel || modelParent === Model || modelParent instanceof Model
      ? getBinderNode(modelParent)
      : undefined;
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

    this.initializeValue();

    const key = this.model[$key];

    // The value of parent in unknown, so we need to cast it.
    type ParentValue = Readonly<Record<typeof key, Value<M>>>;
    return (this.parent.value as ParentValue)[key];
  }

  set value(value: Value<M> | undefined) {
    this.initializeValue(true);
    const oldValue = this.value;
    if (value !== oldValue) {
      this.#setValueState(value, undefined);
      this[_updateValidation]().catch(() => {});
    }
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
  for<N extends ProvisionalModel>(model: N): BinderNode<N> {
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
      const newValue = (this.parent.value ?? []).filter((_, i) => i !== this.model[$key]);
      const newDefaultValue = (this.parent.defaultValue ?? []).filter((_, i) => i !== this.model[$key]);
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
    if (this.invalid) {
      await this.validate();
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
        if (childModel[$key] in (this.defaultValue as Record<never, never>)) {
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
    return this.model[$owner] instanceof ArrayModel;
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

    const key = this.model[$key];
    let value: Value<M> | undefined = this.parent
      ? (this.parent.value as Record<typeof key, Value<M>>)[this.model[$key]]
      : undefined;

    const defaultValue: Value<M> | undefined = this.parent
      ? (this.parent.defaultValue as Readonly<Record<typeof key, Value<M>>>)[this.model[$key]]
      : undefined;

    if (value === undefined) {
      // Initialize value if this is the root level node, or it is enforced
      if (forceInitialize || !this.parent) {
        value = createEmptyValue(this.model) as Value<M>;
        this.#setValueState(value, defaultValue === undefined ? value : defaultValue);
      } else if (
        this.parent.model instanceof ObjectModel &&
        !(key in ((this.parent.value || {}) as Partial<Record<typeof key, Value<M>>>))
      ) {
        this.#setValueState(undefined, defaultValue === undefined ? value : defaultValue);
      }
    }
  }

  #setValueState(value: Value<M> | undefined, defaultValue: Value<M> | undefined): void {
    const { parent } = this;
    if (parent) {
      const key = this.model[$key];
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
