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

export function getBinderNode<M extends AbstractModel<any>, T extends ModelValue<M>>(model: M): BinderNode<T, M> {
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
export type ModelValue<M extends AbstractModel<unknown>> = ReturnType<M['valueOf']>;

export interface ModelConstructor<T, M extends AbstractModel<T>> {
  new (parent: ModelParent<T>, key: keyof any, optional: boolean, ...args: any[]): M;
  createEmptyValue(): T;
}

type ModelVariableArguments<C> = C extends new (
  parent: ModelParent<any>,
  key: keyof any,
  ...args: infer R extends [boolean, ...any]
) => AbstractModel<any>
  ? R
  : never;

type ModelCtor = {
  new (parent: ModelParent<any>, key: keyof any, ...args: any[]): any;
  createEmptyValue(): unknown;
};

export type ModelInstance<C extends ModelCtor, MArgs extends ModelVariableArguments<C>> = C extends new (
  parent: ModelParent<any>,
  key: keyof any,
  ...args: MArgs
) => infer M
  ? M
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

  toString(): string {
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
  static override createEmptyValue(): unknown {
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

  static override createEmptyValue(): { readonly [key in never]: unknown } {
    const model = new this({ value: undefined as any }, 'value' as keyof any, false);
    const obj: Record<string, unknown> = {};

    // Iterate the model class hierarchy up to the ObjectModel, and extract
    // the property getter names from every prototypes
    for (const [key, getter] of this.getOwnAndParentGetters(model)) {
      const propertyModel = getter.call(model) as AbstractModel<any>;
      obj[key] = propertyModel[_optional] ? undefined : propertyModel.constructor.createEmptyValue();
    }

    return obj;
  }

  private [_properties]: { [name in keyof T]?: AbstractModel<any> } = {};

  protected [_getPropertyModel]<
    KChild extends keyof T,
    C extends ModelCtor,
    MArgs extends ModelVariableArguments<C>,
    M extends ModelInstance<C, MArgs>,
  >(key: KChild, ValueModel: C, valueModelArgs: MArgs): M {
    if (this[_properties][key] === undefined) {
      this[_properties][key] = new ValueModel(this, key, ...valueModelArgs);
    }

    return this[_properties][key]! as M;
  }
}

export class ArrayModel<
  T extends ModelValue<M>,
  M extends ModelInstance<C, MArgs>,
  C extends ModelCtor = ModelConstructor<T, M>,
  MArgs extends ModelVariableArguments<C> = ModelVariableArguments<C>,
> extends AbstractModel<readonly T[]> {
  static override createEmptyValue(): [] {
    return [];
  }

  private readonly [_ItemModel]: C;

  private readonly itemModelArgs: MArgs;

  private readonly itemModels: M[] = [];

  constructor(
    parent: ModelParent<readonly T[]>,
    key: keyof any,
    optional: boolean,
    ItemModel: C,
    itemModelArgs: MArgs,
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
      let itemModel: M | undefined = this.itemModels[i];
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      if (itemModel === undefined) {
        // const [optional, ...rest] = this.itemModelArgs;
        itemModel = new ItemModel(this, i, ...this.itemModelArgs);
        // @ts-expect-error M always matches the expected type
        this.itemModels[i] = itemModel;
      }
      yield getBinderNode(itemModel!) as unknown as BinderNode<T, M>;
    }
  }
}
