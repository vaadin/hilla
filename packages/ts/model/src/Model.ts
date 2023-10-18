import type { ModelMetadata } from './ModelMetadata.js';

export const _name = Symbol('name');
export const _owner = Symbol('owner');
export const _key = Symbol('key');
export const _meta = Symbol('meta');
export const _value = Symbol('value');
export const _optional = Symbol('optional');

export interface ModelOwner {
  model?: IModel;
}

export const detachedModelOwner: ModelOwner = {
  model: undefined,
};

Object.defineProperty(detachedModelOwner, 'toString', {
  enumerable: false,
  value: () => ':detached:',
});

export interface IModel<T = unknown> {
  [_name]: string;
  [_owner]: IModel | ModelOwner;
  [_key]: keyof any;
  [_value]: T;
  [_meta]: ModelMetadata;
  [_optional]: boolean;
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
