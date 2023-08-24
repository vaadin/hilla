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

import type { Binder } from './Binder.js';
// eslint-disable-next-line import/no-cycle
import {
  _binderNode,
  _ItemModel,
  _key,
  _parent,
  _validators,
  AbstractModel,
  ArrayModel,
  getBinderNode,
  ObjectModel,
} from './Models.js';
import type { Validator, ValueError } from './Validation.js';
import { ValidityStateValidator } from './Validators.js';
import { _validity } from './Validity.js';

const _ownErrors = Symbol('ownErrorsSymbol');
const _visited = Symbol('visited');

function getErrorPropertyName(valueError: ValueError<any>): string {
  return typeof valueError.property === 'string' ? valueError.property : getBinderNode(valueError.property).name;
}

type ArrayBinderNode<TItem> = BinderNode<readonly TItem[], ArrayModel<TItem, AbstractModel<TItem>>>;

type ArrayItemBinderNode<T, M extends AbstractModel<T>> = Omit<BinderNode<T, M>, 'parent'> & {
  parent: ArrayBinderNode<T>;
};

const defaultArrayItemCache = new WeakMap<object, unknown>();
const getDefaultArrayItem = <TItem>(arrayNode: ArrayBinderNode<TItem>): TItem => {
  const cache = defaultArrayItemCache as WeakMap<typeof arrayNode, TItem>;
  if (cache.has(arrayNode)) {
    return defaultArrayItemCache.get(arrayNode) as TItem;
  }
  const defaultArrayItem = arrayNode.model[_ItemModel].createEmptyValue();
  cache.set(arrayNode, defaultArrayItem);
  return defaultArrayItem;
};

export const CHANGED = new Event('binder-node-changed');

/**
 * The BinderNode\<T, M\> class provides the form binding related APIs
 * with respect to a particular model instance.
 *
 * Structurally, model instances form a tree, in which the object
 * and array models have child nodes of field and array item model
 * instances.
 */
export class BinderNode<T, M extends AbstractModel<T>> extends EventTarget {
  readonly model: M;

  /**
   * The validity state read from the bound element, if any. Represents the
   * HTML element internal validation.
   *
   * For elements with `validity.valid === false`, the value in the
   * bound element is considered as invalid.
   */
  [_validity]?: ValidityState;

  private [_visited] = false;

  private [_validators]: ReadonlyArray<Validator<T>>;

  private [_ownErrors]?: ReadonlyArray<ValueError<T>>;

  private readonly validityStateValidator: ValidityStateValidator<T>;

  constructor(model: M) {
    super();
    this.model = model;
    model[_binderNode] = this;
    this.validityStateValidator = new ValidityStateValidator<T>();
    this.initializeValue();
    this[_validators] = model[_validators];
  }

  /**
   * The parent node, if this binder node corresponds to a nested model,
   * otherwise undefined for the top-level binder.
   */
  get parent(): BinderNode<unknown, AbstractModel<unknown>> | undefined {
    const modelParent = this.model[_parent];
    return modelParent instanceof AbstractModel ? getBinderNode(modelParent) : undefined;
  }

  /**
   * The binder for the top-level model.
   */
  get binder(): Binder<unknown, AbstractModel<unknown>> {
    return this.parent ? this.parent.binder : (this as any);
  }

  /**
   * The name generated from the model structure, used to set the name
   * attribute on the field components.
   */
  get name(): string {
    let model = this.model as AbstractModel<any>;
    const strings = [];
    while (model[_parent] instanceof AbstractModel) {
      strings.unshift(String(model[_key]));
      model = model[_parent];
    }
    return strings.join('.');
  }

  /**
   * The current value related to the model
   */
  get value(): T | undefined {
    if (this.parent!.value === undefined) {
      this.parent!.initializeValue(true);
    }
    const key = this.model[_key];
    return (this.parent!.value as { readonly [key in typeof key]: T })[key];
  }

  set value(value: T | undefined) {
    this.setValueState(value);
  }

  /**
   * The default value related to the model
   */
  get defaultValue(): T {
    if (this.isArrayItem()) {
      const arrayNode = this.parent.asArray<T>();
      return getDefaultArrayItem(arrayNode);
    }

    const key = this.model[_key];
    return (this.parent!.defaultValue as { readonly [key in typeof key]: T })[key];
  }

  /**
   * True if the current value is different from the defaultValue.
   */
  get dirty(): boolean {
    return this.value !== this.defaultValue;
  }

  /**
   * The array of validators for the model. The default value is defined in the
   * model.
   */
  get validators(): ReadonlyArray<Validator<T>> {
    return this[_validators];
  }

  set validators(validators: ReadonlyArray<Validator<T>>) {
    this[_validators] = validators;
    this.dispatchEvent(CHANGED);
  }

  /**
   * True if the bound field was ever focused and blurred by the user.
   */
  get visited(): boolean {
    return this[_visited];
  }

  set visited(v: boolean) {
    if (this[_visited] !== v) {
      this[_visited] = v;
      this.updateValidation().catch(() => {});
      this.dispatchEvent(CHANGED);
    }
  }

  /**
   * The combined array of all errors for this node’s model and all its nested
   * models
   */
  get errors(): ReadonlyArray<ValueError<any>> {
    const descendantsErrors = [...this.getChildBinderNodes()].reduce<readonly any[]>(
      (errors, childBinderNode) => [...errors, ...childBinderNode.errors],
      [],
    );
    return descendantsErrors.concat(this.ownErrors);
  }

  /**
   * The array of validation errors directly related with the model.
   */
  get ownErrors(): ReadonlyArray<ValueError<T>> {
    return this[_ownErrors] ? this[_ownErrors] : [];
  }

  /**
   * Indicates if there is any error for the node's model.
   */
  get invalid(): boolean {
    return this.errors.length > 0;
  }

  /**
   * True if the value is required to be non-empty.
   */
  get required(): boolean {
    return this[_validators].some((validator) => validator.impliesRequired);
  }

  /**
   * Returns a binder node for the nested model instance.
   *
   * @param model - The nested model instance
   */
  for<NM extends AbstractModel<any>>(model: NM): BinderNode<ReturnType<NM['valueOf']>, NM> {
    const binderNode = getBinderNode(model);
    if (binderNode.binder !== this.binder) {
      throw new Error('Unknown binder');
    }

    return binderNode;
  }

  /**
   * Runs all validation callbacks potentially affecting this
   * or any nested model. Returns the combined array of all
   * errors as in the errors property.
   */
  async validate(): Promise<ReadonlyArray<ValueError<any>>> {
    const errors = (
      await Promise.all([...this.requestValidationOfDescendants(), ...this.requestValidationWithAncestors()])
    ).flat();
    this.setErrorsWithDescendants(errors.length ? errors : undefined);
    this.update();
    return errors;
  }

  /**
   * A helper method to add a validator
   *
   * @param validator - a validator
   */
  addValidator(validator: Validator<T>): void {
    this.validators = [...this[_validators], validator];
    this.dispatchEvent(CHANGED);
  }

  /**
   * Append an item to the array value.
   *
   * Requires the context model to be an array reference.
   *
   * @param itemValue - optional new item value, an empty item is
   * appended if the argument is omitted
   */
  appendItem<TItem extends M extends ArrayModel<infer TArrayItem, AbstractModel<any>> ? TArrayItem : never>(
    itemValue?: TItem,
  ): void {
    const arrayNode = this.asArray<TItem>();
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
    const itemValueOrEmptyValue = itemValue ?? arrayNode.model[_ItemModel].createEmptyValue();
    arrayNode.value = [...(arrayNode.value ?? []), itemValueOrEmptyValue];
  }

  /**
   * Prepend an item to the array value.
   *
   * Requires the context model to be an array reference.
   *
   * @param itemValue - optional new item value, an empty item is prepended if
   * the argument is omitted
   */
  prependItem<TItem extends M extends ArrayModel<infer TArrayItem, AbstractModel<any>> ? TArrayItem : never>(
    itemValue?: TItem,
  ): void {
    const arrayNode = this.asArray<TItem>();
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
    const itemValueOrEmptyValue = itemValue ?? arrayNode.model[_ItemModel].createEmptyValue();
    arrayNode.value = [itemValueOrEmptyValue, ...(arrayNode.value ?? [])];
  }

  /**
   * Remove itself from the parent array value.
   *
   * Requires the context model to be an array item reference.
   */
  removeSelf(): void {
    const arrayItemNode = this.asArrayItem();
    const itemIndex = this.model[_key];
    const arrayNode = arrayItemNode.parent;
    arrayNode.value = (arrayNode.value ?? []).filter((_, i) => i !== itemIndex);
  }

  protected clearValidation(): boolean {
    if (this[_visited]) {
      this[_visited] = false;
      this.dispatchEvent(CHANGED);
    }
    let needsUpdate = false;
    if (this[_ownErrors]) {
      this[_ownErrors] = undefined;
      needsUpdate = true;
      this.dispatchEvent(CHANGED);
    }
    if ([...this.getChildBinderNodes()].filter((childBinderNode) => childBinderNode.clearValidation()).length > 0) {
      needsUpdate = true;
    }
    return needsUpdate;
  }

  protected async updateValidation(): Promise<void> {
    if (this[_visited]) {
      await this.validate();
    } else if (this.dirty || this.invalid) {
      await Promise.all(
        [...this.getChildBinderNodes()].map(async (childBinderNode) => childBinderNode.updateValidation()),
      );
    }
  }

  protected update(_?: T): void {
    if (this.parent) {
      this.parent.update();
    }
  }

  protected setErrorsWithDescendants(errors?: ReadonlyArray<ValueError<any>>): void {
    const { name } = this;
    const ownErrors = errors ? errors.filter((valueError) => getErrorPropertyName(valueError) === name) : undefined;
    const relatedErrors = errors
      ? errors.filter((valueError) => getErrorPropertyName(valueError).startsWith(name))
      : undefined;
    this[_ownErrors] = ownErrors;
    for (const childBinderNode of this.getChildBinderNodes()) {
      childBinderNode.setErrorsWithDescendants(relatedErrors);
    }
    this.dispatchEvent(CHANGED);
  }

  private *getChildBinderNodes(): Generator<BinderNode<unknown, AbstractModel<unknown>>> {
    if (this.value === undefined) {
      // Undefined value cannot have child properties and items.
      return;
    }

    if (this.model instanceof ObjectModel) {
      // We need to skip all non-initialised optional fields here in order to
      // prevent infinite recursion for circular references in the model.
      // Here we rely on presence of keys in `defaultValue` to detect all
      // initialised fields. The keys in `defaultValue` are defined for all
      // non-optional fields plus those optional fields whose values were set
      // from initial `binder.read()` or `binder.clear()` or by using a
      // binder node (e.g., form binding) for a nested field.
      if (this.defaultValue) {
        for (const [, getter] of ObjectModel.getOwnAndParentGetters(this.model)) {
          const childModel = getter.call(this.model);
          if (childModel instanceof AbstractModel) {
            yield getBinderNode(childModel);
          }
        }
      }
    } else if (this.model instanceof ArrayModel) {
      for (const childBinderNode of this.model) {
        yield childBinderNode;
      }
    }
  }

  private runOwnValidators(): ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>> {
    if (this[_validity] && !this[_validity].valid) {
      // The element's internal validation reported invalid state.

      if (this[_validity].badInput) {
        // Bad input means the `value` cannot be used and even meaningfully
        // validated with the validators in the binder, because it cannot be
        // parsed, for example, if a date is entered with incorrect format.
        //
        // Skip running the validators, and instead assume the only error
        // from the validity state.
        return [this.binder.requestValidation(this.model, this.validityStateValidator)];
      }
      // Validate the value, but also raise the error from the validity state.
      return [...this[_validators], this.validityStateValidator].map(async (validator) =>
        this.binder.requestValidation(this.model, validator),
      );
    }

    return this[_validators].map(async (validator) => this.binder.requestValidation(this.model, validator));
  }

  private requestValidationOfDescendants(): ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>> {
    return [...this.getChildBinderNodes()].reduce<ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>>>(
      (promises, childBinderNode) => [
        ...promises,
        ...childBinderNode.runOwnValidators(),
        ...childBinderNode.requestValidationOfDescendants(),
      ],
      [],
    );
  }

  private requestValidationWithAncestors(): ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>> {
    return [...this.runOwnValidators(), ...(this.parent ? this.parent.requestValidationWithAncestors() : [])];
  }

  private initializeValue(requiredByChildNode = false): void {
    // First, make sure parents have value initialized
    if (this.parent && (this.parent.value === undefined || (this.parent.defaultValue as T | undefined) === undefined)) {
      this.parent.initializeValue(true);
    }

    const key = this.model[_key];
    let value: T | undefined = this.parent
      ? (this.parent.value as { readonly [key in typeof key]: T })[this.model[_key]]
      : undefined;

    if (value === undefined) {
      // Initialize value if a child node is accessed or for the root-level node
      if (requiredByChildNode || !this.parent) {
        value = this.model.constructor.createEmptyValue() as T;
        this.setValueState(value, this.defaultValue === undefined);
      } else if (
        this.parent.model instanceof ObjectModel &&
        !(key in ((this.parent.value || {}) as { readonly [key in typeof key]?: T }))
      ) {
        this.setValueState(undefined, this.defaultValue === undefined);
      }
    }
  }

  private setValueState(value: T | undefined, keepPristine = false): void {
    const modelParent = this.model[_parent];
    const key = this.model[_key];
    if (modelParent instanceof ObjectModel) {
      // Value contained in object - replace object in parent
      const object: { readonly [key in typeof key]?: T } = {
        ...(this.parent!.value as { readonly [key in typeof key]?: T }),
        [key]: value,
      };
      this.parent!.setValueState(object, keepPristine);
      return;
    }

    if (value === undefined) {
      throw new TypeError('Unexpected undefined value');
    }

    if (this.isArrayItem()) {
      // Value contained in array - replace array in parent
      const array = (this.parent.value ?? []).slice();
      array[key as number] = value;
      this.parent.setValueState(array, keepPristine);
    } else {
      // Value contained elsewhere, probably binder - use value property setter
      const binder = modelParent as Binder<T, M>;
      if (keepPristine && !binder.dirty) {
        binder.defaultValue = value;
      }
      binder.value = value!;
    }
  }

  private isArray<TItem>(): this is ArrayBinderNode<TItem> {
    return this.model instanceof ArrayModel;
  }

  private asArray<TItem>(): ArrayBinderNode<TItem> {
    if (!this.isArray()) {
      throw new TypeError('Model is not array');
    }

    return this as ArrayBinderNode<TItem>;
  }

  private isArrayItem(): this is ArrayItemBinderNode<T, M> {
    return this.parent?.model instanceof ArrayModel;
  }

  private asArrayItem(): ArrayItemBinderNode<T, M> {
    if (!this.isArrayItem()) {
      throw new TypeError('Model is not an array item');
    }

    return this as ArrayItemBinderNode<T, M>;
  }
}
