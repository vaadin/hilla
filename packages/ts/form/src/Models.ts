// TODO: Fix dependency cycle

import isNumeric from 'validator/es/lib/isNumeric.js';
// eslint-disable-next-line import/no-cycle
import { BinderNode } from './BinderNode.js';
import type { Validator } from './Validation.js';
import { IsNumber } from './Validators.js';

export const _ItemModel = Symbol('ItemModel');
export const _parent = Symbol('parent');
export const _key = Symbol('key');
export const _fromString = Symbol('fromString');
export const _validators = Symbol('validators');
export const _binderNode = Symbol('binderNode');
export const _getPropertyModel = Symbol('getPropertyModel');
export const _enum = Symbol('enum');

const _properties = Symbol('properties');
const _optional = Symbol('optional');

export function getBinderNode<M extends AbstractModel<any>, T = ModelValue<M>>(model: M): BinderNode<T, M> {
  if (!model[_binderNode]) {
    model[_binderNode] = new BinderNode(model);
  }

  return model[_binderNode]!;
}

export interface HasFromString<T> {
  [_fromString](value: string): T;
}

export function hasFromString<T>(model: AbstractModel<T>): model is AbstractModel<T> & HasFromString<T> {
  return _fromString in model;
}

export interface HasValue<T> {
  value?: T;
}

export type ModelParent<T> = AbstractModel<any> | HasValue<T>;
export type ModelValue<M extends AbstractModel<any>> = ReturnType<M['valueOf']>;

export interface ModelConstructor<T, M extends AbstractModel<T>> {
  createEmptyValue(): T;
  new (parent: ModelParent<T>, key: keyof any, optional: boolean, ...args: any[]): M;
}

type ModelVariableArguments<C extends ModelConstructor<any, AbstractModel<any>>> = C extends new (
  parent: ModelParent<any>,
  key: keyof any,
  ...args: infer R
) => any
  ? R
  : never;

export abstract class AbstractModel<T> {
  static createEmptyValue(): unknown {
    return undefined;
  }

  declare readonly ['constructor']: typeof AbstractModel;

  readonly [_parent]: ModelParent<T>;

  readonly [_validators]: ReadonlyArray<Validator<T>>;

  readonly [_optional]: boolean;

  [_binderNode]?: BinderNode<T, this>;

  [_key]: keyof any;

  constructor(parent: ModelParent<T>, key: keyof any, optional: boolean, ...validators: ReadonlyArray<Validator<T>>) {
    this[_parent] = parent;
    this[_key] = key;
    this[_optional] = optional;
    this[_validators] = validators;
  }

  toString() {
    return String(this.valueOf());
  }

  valueOf(): T {
    const { value } = getBinderNode(this);
    if (value === undefined) {
      throw new TypeError('Value is undefined');
    }
    return value;
  }
}

export abstract class PrimitiveModel<T> extends AbstractModel<T> {}

export class BooleanModel extends PrimitiveModel<boolean> implements HasFromString<boolean> {
  static override createEmptyValue = Boolean;

  [_fromString](str: string): boolean {
    // This implementation matches the values accepted by validator.js and converts all other values to false
    // See https://github.com/validatorjs/validator.js/blob/master/src/lib/isBoolean.js
    return ['true', '1', 'yes'].includes(str.toLowerCase());
  }
}

export class NumberModel extends PrimitiveModel<number> implements HasFromString<number | undefined> {
  static override createEmptyValue = Number;

  constructor(
    parent: ModelParent<number>,
    key: keyof any,
    optional: boolean,
    ...validators: ReadonlyArray<Validator<number>>
  ) {
    // Prepend a built-in validator to indicate NaN input
    super(parent, key, optional, new IsNumber(optional), ...validators);
  }

  [_fromString](str: string): number | undefined {
    // Returning undefined is needed to support passing the validation when the value of an optional number field is
    // an empty string
    if (str === '') return undefined;
    return isNumeric(str) ? Number.parseFloat(str) : NaN;
  }
}

export class StringModel extends PrimitiveModel<string> implements HasFromString<string> {
  static override createEmptyValue = String;

  [_fromString] = String;
}

declare enum Enum {}

export abstract class EnumModel<E extends typeof Enum>
  extends AbstractModel<E[keyof E]>
  implements HasFromString<E[keyof E] | undefined>
{
  static override createEmptyValue() {
    if (this === EnumModel) {
      throw new Error('Cannot create an instance of an abstract class');
    }

    // @ts-expect-error: the instantiation of the abstract class is handled above.
    // Now only the children instantiation could happen.
    const { [_enum]: enumObject } = new this({ value: undefined }, 'value', false);

    return Object.values(enumObject)[0];
  }

  abstract readonly [_enum]: E;

  [_fromString](value: string): E[keyof E] | undefined {
    return value in this[_enum] ? (value as E[keyof E]) : undefined;
  }
}

export class ObjectModel<T> extends AbstractModel<T> {
  static *getOwnAndParentGetters<M extends ObjectModel<any>>(
    model: M,
  ): Generator<readonly [key: string, getter: () => unknown]> {
    for (
      let proto = Object.getPrototypeOf(model);
      proto !== ObjectModel.prototype;
      proto = Object.getPrototypeOf(proto)
    ) {
      const descriptors = Object.getOwnPropertyDescriptors(proto);
      for (const [name, { get }] of Object.entries(descriptors)) {
        if (get) {
          yield [name, get];
        }
      }
    }
  }

  static override createEmptyValue() {
    const model = new this({ value: undefined }, 'value', false);
    const obj: Record<string, unknown> = {};

    // Iterate the model class hierarchy up to the ObjectModel, and extract
    // the property getter names from every prototypes
    for (const [key, getter] of this.getOwnAndParentGetters(model)) {
      const propertyModel = getter.call(model) as AbstractModel<any>;
      obj[key] = propertyModel[_optional] ? undefined : propertyModel.constructor.createEmptyValue();
    }

    return obj as unknown;
  }

  private [_properties]: { [name in keyof T]?: AbstractModel<T[name]> } = {};

  protected [_getPropertyModel]<
    N extends keyof T,
    C extends new (parent: ModelParent<NonNullable<T[N]>>, key: keyof any, optional: boolean, ...args: any[]) => any,
  >(name: N, ValueModel: C, valueModelArgs: any[]): InstanceType<C> {
    const [optional, ...rest] = valueModelArgs;

    if (this[_properties][name] === undefined) {
      this[_properties][name] = new ValueModel(this, name, optional, ...rest);
    }

    return this[_properties][name] as InstanceType<C>;
  }
}

export class ArrayModel<T, M extends AbstractModel<T>> extends AbstractModel<readonly T[]> {
  static override createEmptyValue() {
    return [] as readonly unknown[];
  }

  private readonly [_ItemModel]: ModelConstructor<T, M>;

  private readonly itemModelArgs: readonly any[];

  private readonly itemModels: M[] = [];

  constructor(
    parent: ModelParent<readonly T[]>,
    key: keyof any,
    optional: boolean,
    ItemModel: ModelConstructor<T, M>,
    itemModelArgs: ModelVariableArguments<typeof ItemModel>,
    ...validators: ReadonlyArray<Validator<readonly T[]>>
  ) {
    super(parent, key, optional, ...validators);
    this[_ItemModel] = ItemModel;
    this.itemModelArgs = itemModelArgs;
  }

  /**
   * Iterates the current array value and yields a binder node for every item.
   */
  *[Symbol.iterator](): IterableIterator<BinderNode<T, M>> {
    const array = this.valueOf();
    const ItemModel = this[_ItemModel];
    if (array.length !== this.itemModels.length) {
      this.itemModels.length = array.length;
    }
    for (const i of array.keys()) {
      let itemModel = this.itemModels[i];
      if (!itemModel) {
        const [optional, ...rest] = this.itemModelArgs;
        itemModel = new ItemModel(this, i, optional, ...rest);
        this.itemModels[i] = itemModel;
      }
      yield getBinderNode(itemModel);
    }
  }
}
