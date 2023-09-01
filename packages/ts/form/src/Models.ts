// TODO: Fix dependency cycle
import isNumeric from 'validator/es/lib/isNumeric.js';
// eslint-disable-next-line import/no-cycle
import { type BinderNode, getBinderNode } from './BinderNode.js';
import type { BinderRoot } from './BinderRoot.js';
import type { Validator } from './Validation.js';
import { IsNumber } from './Validators.js';

export const _createDefaultValue = Symbol('itemModel');
export const _parent = Symbol('parent');
export const _key = Symbol('key');
export const _fromString = Symbol('fromString');
export const _validators = Symbol('validators');
export const _getPropertyModel = Symbol('getPropertyModel');
export const _enum = Symbol('enum');

const _properties = Symbol('properties');
const _optional = Symbol('optional');

export interface HasFromString<T> {
  [_fromString](value: string): T;
}

export function hasFromString<T>(model: AbstractModel<T>): model is AbstractModel<T> & HasFromString<T> {
  return _fromString in model;
}

export interface HasValue<T> {
  value?: T;
}

export type Value<M> = M extends AbstractModel<infer T> ? T : never;

export interface ModelConstructor<M extends AbstractModel> {
  new (parent: AbstractModel | undefined, key: keyof any, optional: boolean, ...args: any[]): M;
  createEmptyValue(): Value<M>;
}

export abstract class AbstractModel<T = unknown> {
  static createEmptyValue(): unknown {
    return undefined;
  }

  declare readonly ['constructor']: typeof AbstractModel;

  readonly [_parent]?: AbstractModel | BinderRoot;

  readonly [_validators]: ReadonlyArray<Validator<T>>;

  readonly [_optional]: boolean;

  [_key]: keyof any;

  constructor(
    parent: AbstractModel | BinderRoot | undefined,
    key: keyof any,
    optional: boolean,
    ...validators: ReadonlyArray<Validator<T>>
  ) {
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

    if (!value) {
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
    parent: AbstractModel,
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
    const { [_enum]: enumObject } = new this(undefined, 'value', false);

    return Object.values(enumObject)[0];
  }

  abstract readonly [_enum]: E;

  [_fromString](value: string): E[keyof E] | undefined {
    return value in this[_enum] ? (value as E[keyof E]) : undefined;
  }
}

export class ObjectModel<T extends Record<never, never> = Record<never, never>> extends AbstractModel<T> {
  static *getOwnAndParentGetters<M extends ObjectModel<any>>(
    model: M,
  ): Generator<readonly [key: string, getter: () => AbstractModel]> {
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
    const model = new this(undefined, 'value', false);
    const obj: Record<string, unknown> = {};

    // Iterate the model class hierarchy up to the ObjectModel, and extract
    // the property getter names from every prototypes
    for (const [key, getter] of this.getOwnAndParentGetters(model)) {
      const propertyModel = getter.call(model);
      obj[key] = propertyModel[_optional] ? undefined : propertyModel.constructor.createEmptyValue();
    }

    return obj;
  }

  private [_properties]: { [K in keyof T]?: AbstractModel<T[K]> } = {};

  protected [_getPropertyModel]<K extends keyof T, M extends AbstractModel<T[K]>>(
    key: K,
    init: (parent: this, key: keyof any) => M,
  ): M {
    if (!this[_properties][key]) {
      this[_properties][key] = init(this, key);
    }

    return this[_properties][key] as M;
  }
}

export type ArrayItemModel<M> = M extends ArrayModel<infer MItem> ? MItem : never;

export class ArrayModel<MItem extends AbstractModel = AbstractModel> extends AbstractModel<
  ReadonlyArray<Value<MItem>>
> {
  static override createEmptyValue(): [] {
    return [];
  }

  [_createDefaultValue]: () => Value<MItem>;

  readonly #init: (parent: this, index: number) => MItem;

  #items: Array<MItem | undefined> = [];

  constructor(
    parent: AbstractModel,
    key: keyof any,
    optional: boolean,
    init: (parent: ArrayModel<MItem>, index: number) => MItem,
    createDefaultValue: () => Value<MItem>,
    ...validators: ReadonlyArray<Validator<ReadonlyArray<Value<MItem>>>>
  ) {
    super(parent, key, optional, ...validators);
    this.#init = init;
    this[_createDefaultValue] = createDefaultValue;
  }

  /**
   * Iterates the current array value and yields a binder node for every item.
   */
  *[Symbol.iterator](): IterableIterator<BinderNode<MItem>> {
    const array = this.valueOf();

    if (array.length !== this.#items.length) {
      this.#items.length = array.length;
    }

    for (let i = 0; i < array.length; i++) {
      let item: MItem | undefined = this.#items[i];

      if (!item) {
        item = this.#init(this, i);
        this.#items[i] = item;
      }

      yield getBinderNode(item);
    }
  }
}
