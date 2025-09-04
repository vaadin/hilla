/**
 * TypeScript types and utilities for Spring Data pagination support in Hilla.
 *
 * This module provides TypeScript interfaces that map to the Java mapped types
 * in com.vaadin.hilla.mappedtypes (Pageable, Sort, Order, Slice, Page) and
 * enhances them with array-like access behavior for backwards compatibility.
 *
 * Note: The array-like access behavior (page[0], page.length) is provided for backwards
 * compatibility and may be deprecated in future versions. Consider using page.content directly
 * for accessing the actual data array.
 */

/**
 * Direction values for sorting.
 */
export const Direction = {
  ASC: 'ASC',
  DESC: 'DESC',
} as const;

export type Direction = (typeof Direction)[keyof typeof Direction];

/**
 * Null handling values for sorting.
 */
export const NullHandling = {
  NATIVE: 'NATIVE',
  NULLS_FIRST: 'NULLS_FIRST',
  NULLS_LAST: 'NULLS_LAST',
} as const;

export type NullHandling = (typeof NullHandling)[keyof typeof NullHandling];

/**
 * A DTO for org.springframework.data.domain.Sort.Order.
 * Maps to com.vaadin.hilla.mappedtypes.Order
 */
export interface Order {
  direction: Direction;
  property: string;
  ignoreCase: boolean;
  nullHandling: NullHandling;
}

/**
 * A DTO for org.springframework.data.domain.Sort.
 * Maps to com.vaadin.hilla.mappedtypes.Sort
 */
export interface Sort {
  orders: Order[];
}

/**
 * A DTO for org.springframework.data.domain.Pageable.
 * Maps to com.vaadin.hilla.mappedtypes.Pageable
 */
export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: Sort;
}

/**
 * A DTO for org.springframework.data.domain.Slice.
 * Maps to com.vaadin.hilla.mappedtypes.Slice
 */
export interface Slice<T> extends Iterable<T> {
  content: T[];
  number: number;
  numberOfElements: number;
  size: number;
  sort: Sort;
  hasContent: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  first: boolean;
  last: boolean;
  empty: boolean;
  [index: number]: T;

  // Array-like methods
  length: number;
  map<U>(callback: (value: T, index: number, array: T[]) => U): U[];
  filter(callback: (value: T, index: number, array: T[]) => boolean): T[];
  find(callback: (value: T, index: number, array: T[]) => boolean): T | undefined;
}

/**
 * A DTO for org.springframework.data.domain.Page.
 * Maps to com.vaadin.hilla.mappedtypes.Page
 */
export interface Page<T> extends Slice<T> {
  totalElements: number;
  totalPages: number;
}

/**
 * Base type for objects that might be Slice-like from server responses
 */
type UnknownSliceLike = {
  content?: unknown;
  number?: unknown;
  size?: unknown;
  hasNext?: unknown;
  first?: unknown;
  totalElements?: unknown;
  totalPages?: unknown;
};

/**
 * Checks if an object is a Slice by looking for required properties.
 */
export function isSlice(obj: unknown): obj is Slice<unknown> {
  const candidate = obj as UnknownSliceLike;
  return (
    obj !== null &&
    obj !== undefined &&
    typeof obj === 'object' &&
    Array.isArray(candidate.content) &&
    typeof candidate.number === 'number' &&
    typeof candidate.size === 'number' &&
    typeof candidate.hasNext === 'boolean' &&
    typeof candidate.first === 'boolean'
  );
}

/**
 * Checks if an object is a Page by looking for required properties.
 */
export function isPage(obj: unknown): obj is Page<unknown> {
  const candidate = obj as UnknownSliceLike;
  return isSlice(obj) && typeof candidate.totalElements === 'number' && typeof candidate.totalPages === 'number';
}

/**
 * Enhances a Slice object with array-like access behavior.
 * Can be used in JSON reviver or to enhance server responses.
 */
export function enhanceSlice<T>(slice: Slice<T>): Slice<T> {
  const enhanced = {
    ...slice,

    [Symbol.iterator](): Iterator<T> {
      return slice.content[Symbol.iterator]();
    },

    get length(): number {
      return slice.content.length;
    },

    map<U>(callback: (value: T, index: number, array: T[]) => U): U[] {
      return slice.content.map(callback);
    },

    filter(callback: (value: T, index: number, array: T[]) => boolean): T[] {
      return slice.content.filter(callback);
    },

    find(callback: (value: T, index: number, array: T[]) => boolean): T | undefined {
      return slice.content.find(callback);
    },
  };

  return new Proxy(enhanced, {
    get(target, prop) {
      if (typeof prop === 'string' && !isNaN(Number(prop))) {
        return target.content[Number(prop)];
      }
      return target[prop as keyof typeof target];
    },
  }) as Slice<T>;
}

/**
 * Enhances a Page object with array-like access behavior.
 * Can be used in JSON reviver or to enhance server responses.
 */
export function enhancePage<T>(page: Page<T>): Page<T> {
  const enhancedSlice = enhanceSlice(page);

  const enhanced = {
    ...enhancedSlice,
    totalElements: page.totalElements,
    totalPages: page.totalPages,
  };

  return new Proxy(enhanced, {
    get(target, prop) {
      if (typeof prop === 'string' && !isNaN(Number(prop))) {
        return target.content[Number(prop)];
      }
      return target[prop as keyof typeof target];
    },
  }) as Page<T>;
}
