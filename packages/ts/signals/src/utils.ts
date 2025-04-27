export interface PropertyDescriptor<T = unknown> {
  configurable?: boolean;
  enumerable?: boolean;
  value?: T;
  writable?: boolean;
  get?(): T;
  set?(v: T): void;
}

export type PropertyDescriptorMap<T extends object> = Readonly<{
  [K in keyof T]: PropertyDescriptor<T[K]> & ThisType<any>;
}>;

export interface GenericObjectConstructor {
  create<T extends object | null>(o: T): T;
  create<T extends object | null, EX extends object>(o: T, properties: PropertyDescriptorMap<EX>): T & EX;
  defineProperty<T, P extends PropertyKey, V>(
    o: T,
    p: P,
    attributes: PropertyDescriptor<V> & ThisType<any>,
  ): T & Record<P, V>;
  keys<T extends object>(o: T): ReadonlyArray<keyof T>;
  values<T extends object>(o: T): ReadonlyArray<T[keyof T]>;
  entries<T extends object>(o: T): ReadonlyArray<[keyof T, T[keyof T]]>;
  fromEntries<K extends PropertyKey, V>(entries: Iterable<readonly [K, V]>): Readonly<Record<K, V>>;
}

export interface GenericArray {
  isArray<T>(arg: unknown): arg is T[];
}

// eslint-disable-next-line @typescript-eslint/unbound-method
export const { create, defineProperty, entries, fromEntries, keys } = Object as GenericObjectConstructor;
// eslint-disable-next-line @typescript-eslint/unbound-method
export const { isArray } = Array as GenericArray;
