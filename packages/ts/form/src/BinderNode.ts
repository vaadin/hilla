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
  ModelConstructor,
  ModelValue,
  ObjectModel,
} from './Models.js';
import type { Validator, ValueError } from './Validation.js';

const _ownErrors = Symbol('ownErrorsSymbol');
const _visited = Symbol('visited');

function getErrorPropertyName(valueError: ValueError<any>): string {
  return typeof valueError.property === 'string' ? valueError.property : getBinderNode(valueError.property).name;
}

/**
 * The BinderNode<T, M> class provides the form binding related APIs
 * with respect to a particular model instance.
 *
 * Structurally, model instances form a tree, in which the object
 * and array models have child nodes of field and array item model
 * instances.
 */
export class BinderNode<T, M extends AbstractModel<T>> {
  public readonly model: M;

  private [_visited] = false;

  private [_validators]: ReadonlyArray<Validator<T>>;

  private [_ownErrors]?: ReadonlyArray<ValueError<T>>;

  private defaultArrayItemValue?: T;

  public constructor(model: M) {
    this.model = model;
    model[_binderNode] = this;
    this.initializeValue();
    this[_validators] = model[_validators];
  }

  /**
   * The parent node, if this binder node corresponds to a nested model,
   * otherwise undefined for the top-level binder.
   */
  public get parent(): BinderNode<any, AbstractModel<any>> | undefined {
    const modelParent = this.model[_parent];
    return modelParent instanceof AbstractModel ? getBinderNode(modelParent) : undefined;
  }

  /**
   * The binder for the top-level model.
   */
  public get binder(): Binder<any, AbstractModel<any>> {
    return this.parent ? this.parent.binder : (this as any);
  }

  /**
   * The name generated from the model structure, used to set the name
   * attribute on the field components.
   */
  public get name(): string {
    let model = this.model as AbstractModel<any>;
    const strings = [];
    while (model[_parent] instanceof AbstractModel) {
      strings.unshift(String(model[_key]));
      model = model[_parent] as AbstractModel<any>;
    }
    return strings.join('.');
  }

  /**
   * The current value related to the model
   */
  public get value(): T | undefined {
    if (this.parent!.value === undefined) {
      this.parent!.initializeValue(true);
    }
    return this.parent!.value[this.model[_key]];
  }

  public set value(value: T | undefined) {
    this.setValueState(value);
  }

  /**
   * The default value related to the model
   */
  public get defaultValue(): T {
    if (this.parent && this.parent.model instanceof ArrayModel) {
      if (!this.parent.defaultArrayItemValue) {
        this.parent.defaultArrayItemValue = this.parent.model[_ItemModel].createEmptyValue();
      }

      return this.parent.defaultArrayItemValue;
    }

    return this.parent!.defaultValue[this.model[_key]];
  }

  /**
   * True if the current value is different from the defaultValue.
   */
  public get dirty(): boolean {
    return this.value !== this.defaultValue;
  }

  /**
   * The array of validators for the model. The default value is defined in the
   * model.
   */
  public get validators(): ReadonlyArray<Validator<T>> {
    return this[_validators];
  }

  public set validators(validators: ReadonlyArray<Validator<T>>) {
    this[_validators] = validators;
  }

  /**
   * Returns a binder node for the nested model instance.
   *
   * @param model The nested model instance
   */
  public for<NM extends AbstractModel<any>>(model: NM): BinderNode<ReturnType<NM['valueOf']>, NM> {
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
  public async validate(): Promise<ReadonlyArray<ValueError<any>>> {
    // TODO: Replace reduce() with flat() when the following issue is solved
    //  https://github.com/vaadin/flow/issues/8658
    const errors = (
      await Promise.all([...this.requestValidationOfDescendants(), ...this.requestValidationWithAncestors()])
    )
      .reduce((acc, val) => acc.concat(val), [])
      .filter((valueError) => valueError) as ReadonlyArray<ValueError<any>>;
    this.setErrorsWithDescendants(errors.length ? errors : undefined);
    this.update();
    return errors;
  }

  /**
   * A helper method to add a validator
   *
   * @param validator a validator
   */
  public addValidator(validator: Validator<T>) {
    this.validators = [...this[_validators], validator];
  }

  /**
   * True if the bound field was ever focused and blurred by the user.
   */
  public get visited() {
    return this[_visited];
  }

  public set visited(v) {
    if (this[_visited] !== v) {
      this[_visited] = v;
      this.updateValidation();
    }
  }

  /**
   * The combined array of all errors for this node’s model and all its nested
   * models
   */
  public get errors(): ReadonlyArray<ValueError<any>> {
    const descendantsErrors = [...this.getChildBinderNodes()].reduce(
      (errors, childBinderNode) => [...errors, ...childBinderNode.errors],
      [] as ReadonlyArray<any>,
    );
    return descendantsErrors.concat(this.ownErrors);
  }

  /**
   * The array of validation errors directly related with the model.
   */
  public get ownErrors() {
    return this[_ownErrors] ? this[_ownErrors] : [];
  }

  /**
   * Indicates if there is any error for the node's model.
   */
  public get invalid() {
    return this.errors.length > 0;
  }

  /**
   * True if the value is required to be non-empty.
   */
  public get required() {
    return this[_validators].some((validator) => validator.impliesRequired);
  }

  /**
   * Append an item to the array value.
   *
   * Requires the context model to be an array reference.
   *
   * @param itemValue optional new item value, an empty item is
   * appended if the argument is omitted
   */
  public appendItem<IT extends ModelValue<M extends ArrayModel<any, infer IM> ? IM : never>>(itemValue?: IT) {
    if (!(this.model instanceof ArrayModel)) {
      throw new Error('Model is not an array');
    }

    if (!itemValue) {
      itemValue = this.model[_ItemModel].createEmptyValue();
    }
    this.value = [...(this.value as unknown as ReadonlyArray<IT>), itemValue] as unknown as T;
  }

  /**
   * Prepend an item to the array value.
   *
   * Requires the context model to be an array reference.
   *
   * @param itemValue optional new item value, an empty item is prepended if
   * the argument is omitted
   */
  public prependItem<IT extends ModelValue<M extends ArrayModel<any, infer IM> ? IM : never>>(itemValue?: IT) {
    if (!(this.model instanceof ArrayModel)) {
      throw new Error('Model is not an array');
    }

    if (!itemValue) {
      itemValue = this.model[_ItemModel].createEmptyValue();
    }
    this.value = [itemValue, ...(this.value as unknown as ReadonlyArray<IT>)] as unknown as T;
  }

  /**
   * Remove itself from the parent array value.
   *
   * Requires the context model to be an array item reference.
   */
  public removeSelf() {
    if (!(this.model[_parent] instanceof ArrayModel)) {
      throw new TypeError('Model is not an array item');
    }
    const itemIndex = this.model[_key] as number;
    this.parent!.value = (this.parent!.value as ReadonlyArray<T>).filter((_, i) => i !== itemIndex);
  }

  protected clearValidation(): boolean {
    if (this[_visited]) {
      this[_visited] = false;
    }
    let needsUpdate = false;
    if (this[_ownErrors]) {
      this[_ownErrors] = undefined;
      needsUpdate = true;
    }
    if ([...this.getChildBinderNodes()].filter((childBinderNode) => childBinderNode.clearValidation()).length > 0) {
      needsUpdate = true;
    }
    return needsUpdate;
  }

  protected async updateValidation() {
    if (this[_visited]) {
      await this.validate();
    } else if (this.dirty || this.invalid) {
      await Promise.all([...this.getChildBinderNodes()].map((childBinderNode) => childBinderNode.updateValidation()));
    }
  }

  protected update(_?: T): void {
    if (this.parent) {
      this.parent.update();
    }
  }

  protected setErrorsWithDescendants(errors?: ReadonlyArray<ValueError<any>>) {
    const { name } = this;
    const ownErrors = errors ? errors.filter((valueError) => getErrorPropertyName(valueError) === name) : undefined;
    const relatedErrors = errors
      ? errors.filter((valueError) => getErrorPropertyName(valueError).startsWith(name))
      : undefined;
    this[_ownErrors] = ownErrors;
    for (const childBinderNode of this.getChildBinderNodes()) {
      childBinderNode.setErrorsWithDescendants(relatedErrors);
    }
  }

  private *getChildBinderNodes(): Generator<BinderNode<any, AbstractModel<any>>> {
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
      // binder node (e. g., form biding) for a nested field.
      if (this.defaultValue) {
        for (const key of Object.keys(this.defaultValue)) {
          const childModel = (this.model as any)[key] as AbstractModel<any>;
          if (childModel) {
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
    return this[_validators].map((validator) => this.binder.requestValidation(this.model, validator));
  }

  private requestValidationOfDescendants(): ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>> {
    return [...this.getChildBinderNodes()].reduce(
      (promises, childBinderNode) => [
        ...promises,
        ...childBinderNode.runOwnValidators(),
        ...childBinderNode.requestValidationOfDescendants(),
      ],
      [] as ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>>,
    );
  }

  private requestValidationWithAncestors(): ReadonlyArray<Promise<ReadonlyArray<ValueError<any>>>> {
    return [...this.runOwnValidators(), ...(this.parent ? this.parent.requestValidationWithAncestors() : [])];
  }

  private initializeValue(requiredByChildNode = false) {
    // First, make sure parents have value initialized
    if (this.parent && (this.parent.value === undefined || this.parent.defaultValue === undefined)) {
      this.parent.initializeValue(true);
    }

    let value = this.parent ? this.parent.value[this.model[_key]] : undefined;

    if (value === undefined) {
      // Initialize value if a child node is accessed or for the root-level node
      if (requiredByChildNode || !this.parent) {
        value = value !== undefined ? value : (this.model.constructor as ModelConstructor<T, M>).createEmptyValue();
        this.setValueState(value, this.defaultValue === undefined);
      } else if (this.parent && this.parent.model instanceof ObjectModel && !(this.model[_key] in this.parent.value)) {
        this.setValueState(undefined, this.defaultValue === undefined);
      }
    }
  }

  private setValueState(value: T | undefined, keepPristine = false) {
    const modelParent = this.model[_parent];
    if (modelParent instanceof ObjectModel) {
      // Value contained in object - replace object in parent
      const object = {
        ...this.parent!.value,
        [this.model[_key]]: value,
      };
      this.parent!.setValueState(object, keepPristine);
      return;
    }

    if (value === undefined) {
      throw new TypeError('Unexpected undefined value');
    }

    if (modelParent instanceof ArrayModel) {
      // Value contained in array - replace array in parent
      const array = (this.parent!.value as ReadonlyArray<T>).slice();
      array[this.model[_key] as number] = value;
      this.parent!.setValueState(array, keepPristine);
    } else {
      // Value contained elsewhere, probably binder - use value property setter
      const binder = modelParent as Binder<T, M>;
      if (keepPristine && !binder.dirty) {
        binder.defaultValue = value;
      }
      binder.value = value!;
    }
  }
}
