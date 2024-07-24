import isNumeric from 'validator/es/lib/isNumeric.js';
import { type BinderNode, getBinderNode } from './BinderNode.js';
import type { Validator } from './Validation.js';
import { IsNumber } from './Validators.js';

export const _createEmptyItemValue = Symbol('createEmptyItemValue');
export const _parent = Symbol('parent');
export const _key = Symbol('key');
export const _fromString = Symbol('fromString');
export const _validators = Symbol('validators');
export const _meta = Symbol('meta');
export const _getPropertyModel = Symbol('getPropertyModel');
export const _enum = Symbol('enum');
export const _items = Symbol('items');

const _optional = Symbol('optional');

export interface HasFromString<T> {
  [_fromString](value: string): T;
}

export function hasFromString<T>(model: AbstractModel<T>): model is AbstractModel<T> & HasFromString<T> {
  return _fromString in model;
}

export type Value<M> = M extends AbstractModel<infer T> ? T : never;

export const modelDetachedParent = { $value$: undefined };

export type ModelParent = AbstractModel | BinderNode | typeof modelDetachedParent;

export interface Annotation {
  name: string;
  attributes?: Record<string, unknown>;
}

export interface ModelMetadata {
  javaType?: string;
  annotations?: Annotation[];
}

export interface ModelOptions<T> {
  validators?: ReadonlyArray<Validator<T>>;
  meta?: ModelMetadata;
}

export type DetachedModelConstructor<M> = {
  prototype: object;
  new (parent: typeof modelDetachedParent, key: '$value$', optional: boolean): M;
};

export function createDetachedModel<M extends AbstractModel>(type: DetachedModelConstructor<M>): M {
  return new type(modelDetachedParent, '$value$', false);
}

export abstract class AbstractModel<T = unknown> {
  static createEmptyValue(): unknown {
    return undefined;
  }

  declare readonly ['constructor']: typeof AbstractModel<T>;

  readonly [_parent]?: ModelParent;

  readonly [_validators]: ReadonlyArray<Validator<T>>;

  readonly [_meta]: ModelMetadata;

  readonly [_optional]: boolean;

  [_key]: keyof any;

  constructor(parent: ModelParent, key: keyof any, optional: boolean, options?: ModelOptions<T>) {
    this[_parent] = parent;
    this[_key] = key;
    this[_optional] = optional;
    this[_validators] = options?.validators ?? [];
    this[_meta] = options?.meta ?? {};
  }

  /**
   * @deprecated Use {@link BinderNode.value} with string conversion instead
   *
   * @example
   * ```ts
   * const result = String(binder.for(model).value);
   * ```
   */
  toString(): string {
    return String(this.valueOf());
  }

  /**
   * @deprecated Use {@link BinderNode.value} instead
   *
   * @example
   * ```ts
   * const result = binder.for(model).value;
   * ```
   */
  valueOf(): T {
    const { value } = getBinderNode(this);

    if (value === undefined) {
      throw new TypeError('Value is undefined');
    }

    return value! as T;
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
  static override createEmptyValue(): number {
    return NaN;
  }

  constructor(parent: ModelParent, key: keyof any, optional: boolean, options?: ModelOptions<number>) {
    // Prepend a built-in validator to indicate NaN input
    const validators = [new IsNumber(optional), ...(options?.validators ?? [])];
    super(parent, key, optional, { ...options, validators });
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

export function makeEnumEmptyValueCreator<M extends EnumModel>(type: DetachedModelConstructor<M>): () => Value<M> {
  const { [_enum]: enumObject } = createDetachedModel(type);
  const defaultValue = Object.values(enumObject)[0] as Value<M>;

  return () => defaultValue;
}

export abstract class EnumModel<E extends typeof Enum = typeof Enum>
  extends AbstractModel<E[keyof E]>
  implements HasFromString<E[keyof E] | undefined>
{
  abstract readonly [_enum]: E;

  [_fromString](value: string): E[keyof E] | undefined {
    return value in this[_enum] ? (value as E[keyof E]) : undefined;
  }
}

export function* getObjectModelOwnAndParentGetters<M extends ObjectModel>(
  model: M,
): Generator<readonly [key: keyof Value<M>, getter: () => AbstractModel]> {
  for (
    let proto = Object.getPrototypeOf(model);
    proto !== ObjectModel.prototype;
    proto = Object.getPrototypeOf(proto)
  ) {
    const descriptors = Object.getOwnPropertyDescriptors(proto);
    for (const [name, { get }] of Object.entries(descriptors)) {
      if (get) {
        yield [name as keyof Value<M>, get];
      }
    }
  }
}

export function makeObjectEmptyValueCreator<M extends ObjectModel>(type: DetachedModelConstructor<M>): () => Value<M> {
  const model = createDetachedModel(type);

  return () => {
    // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
    const obj: Partial<Value<M>> = {};

    // Iterate the model class hierarchy up to the ObjectModel, and extract
    // the property getter names from every prototypes
    for (const [key, getter] of getObjectModelOwnAndParentGetters(model)) {
      const propertyModel = getter.call(model);
      obj[key] = (
        propertyModel[_optional] ? undefined : propertyModel.constructor.createEmptyValue()
      ) as Value<M>[keyof Value<M>];
    }

    return obj as Value<M>;
  };
}

type ChildModel<T extends Record<never, never>, K extends keyof T> = AbstractModel<NonNullable<T[K]>>;

export class ObjectModel<T extends Record<never, never> = Record<never, never>> extends AbstractModel<T> {
  static override createEmptyValue = makeObjectEmptyValueCreator(ObjectModel);

  #properties: { [K in keyof T]?: ChildModel<T, K> } = {};

  protected [_getPropertyModel]<K extends keyof T, M extends ChildModel<T, K>>(
    key: K,
    init: (parent: this, key: K) => M,
  ): M {
    if (!this.#properties[key]) {
      this.#properties[key] = init(this, key);
    }

    return this.#properties[key] as M;
  }
}

export type ArrayItemModel<M> = M extends ArrayModel<infer MItem> ? MItem : never;

export class ArrayModel<MItem extends AbstractModel = AbstractModel> extends AbstractModel<Array<Value<MItem>>> {
  static override createEmptyValue(): [] {
    return [];
  }

  [_createEmptyItemValue]: () => Value<MItem>;

  // The `parent` parameter is AbstractModel here for purpose; for some reason, setting it to `ArrayModel<MItem>` or
  // `this` breaks the type inference in TS (v5.3.2).
  readonly #createItem: (parent: AbstractModel, index: number) => MItem;
  #items: Array<MItem | undefined> = [];

  constructor(
    parent: ModelParent,
    key: keyof any,
    optional: boolean,
    createItem: (parent: AbstractModel, key: number) => MItem,
    options?: ModelOptions<Array<Value<MItem>>>,
  ) {
    super(parent, key, optional, options);
    this.#createItem = createItem;
    this[_createEmptyItemValue] = createItem(this, 0).constructor.createEmptyValue as () => Value<MItem>;
  }

  *[_items](): Generator<MItem, void, void> {
    const values = getBinderNode(this).value ?? [];

    if (values.length !== this.#items.length) {
      this.#items.length = values.length;
    }

    for (let i = 0; i < values.length; i++) {
      let item: MItem | undefined = this.#items[i];

      if (!item) {
        item = this.#createItem(this, i);
        this.#items[i] = item;
      }

      yield item;
    }
  }

  /**
   * Iterates over the current model and yields a binder node for every item
   * model.
   *
   * @deprecated Use the {@link m.items} function instead. For example, in React:
   * ```tsx
   * const {model, field} = useForm(GroupModel);
   * return Array.from(m.items(model.people), (personModel) =>
   *   <TextField label="Full name" {...field(personModel.fullName)} />
   * );
   * ```
   * In Lit:
   * ```ts
   * return html`${repeat(
   *   m.items(this.binder.model.people),
   *   (personModel) => html`<vaadin-text-field label="Full name" ${field(personModel.fullName)}></vaadin-text-field>`,
   * )}`;
   * ```
   */
  *[Symbol.iterator](): IterableIterator<BinderNode<MItem>> {
    for (const item of this[_items]()) {
      yield getBinderNode(item);
    }
  }
}

export const m = {
  /**
   * Returns an iterator over item models in the array model.
   *
   * @param model - The array model to iterate over.
   * @returns An iterator over item models.
   */
  items<M extends ArrayModel>(model: M): Generator<ArrayItemModel<M>, void, void> {
    return model[_items]() as Generator<ArrayItemModel<M>, void, void>;
  },
};
