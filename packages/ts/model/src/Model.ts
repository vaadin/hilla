import type { ModelMetadata } from './ModelMetadata.js';

export const _name = Symbol('name');
export const _owner = Symbol('owner');
export const _key = Symbol('key');
export const _meta = Symbol('meta');
export const _value = Symbol('value');
export const _optional = Symbol('optional');

/**
 * The model hierarchy root type
 */
export interface ModelOwner {
  model?: IModel;
}

/**
 * The defaut container for detached models
 */
export const detachedModelOwner: ModelOwner = {
  model: undefined,
};

Object.defineProperty(detachedModelOwner, 'toString', {
  enumerable: false,
  value: () => ':detached:',
});

/**
 * The base interface for Hilla data models
 */
export interface IModel<T = unknown> {
  /**
   * String name for debug output
   */
  readonly [_name]: string;

  /**
   * Container model or hierarchy root
   */
  readonly [_owner]: IModel | ModelOwner;

  /**
   * The key in the container (property name for object, or index number for arrays).
   */
  readonly [_key]: keyof any;

  /**
   * Value getter and type marker
   */
  readonly [_value]: T;

  /**
   * Optional marker
   */
  readonly [_optional]: boolean;

  /**
   * Other metadata (validation rules, JVM type and annotations, etc) in JSON-like structure
   */
  readonly [_meta]: ModelMetadata;
}

export type Value<M extends IModel> = M extends IModel<infer T> ? T : never;

export const AbstractModel: IModel = Object.create(null, {
  [_name]: {
    value: 'AbstractModel',
    enumerable: false,
  },
  [_owner]: {
    value: detachedModelOwner,
    enumerable: false,
  },
  [_key]: {
    value: 'model',
    enumerable: false,
  },
  [_value]: {
    value: undefined,
    enumerable: false,
  },
  [_meta]: {
    value: {},
    enumerable: false,
  },
  [_optional]: {
    value: false,
    enumerable: false,
  },
  toString: {
    value(this: IModel) {
      return `${String(this[_owner])} / ${String(this[_key])}${this[_optional] ? '?' : ''}: ${this[_name]}`;
    },
    enumerable: false,
  },
});
