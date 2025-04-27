/* eslint-disable @typescript-eslint/no-use-before-define,@typescript-eslint/no-shadow */
import { type Signal, signal as createRawSignal } from '@preact/signals-core';
import type { EmptyObject } from 'type-fest';
import { create, defineProperty, entries, fromEntries, isArray, keys, type PropertyDescriptorMap } from './utils.js';

export { effect, batch, computed } from '@preact/signals-core';

export type AnyObject = Readonly<Record<never, never>>;
export type PrimitiveType = string | number | boolean | null | undefined | symbol | bigint;
export type Value<S extends NodeSignal> = S extends NodeSignal<infer T> ? T : never;

type NodeSignalValueAccessors<V = unknown> = Readonly<{
  get(this: NodeSignal): V;
  set?(this: NodeSignal, value: V): void;
}>;

const idRegistry = new WeakMap<NodeSignal, string>();
const valueRegistry = new WeakMap<NodeSignal, NodeSignalValueAccessors>();
const rawSignalRegistry = new WeakMap<NodeSignal, Signal>();

export type NodeSignal<V = unknown> = Readonly<{
  [Symbol.hasInstance](value: unknown): value is NodeSignal;
}> &
  (V extends ReadonlyArray<infer T>
    ? Readonly<
        Pick<T[], 'push' | 'unshift'> &
          Pick<Array<NodeSignal<T>>, 'pop' | 'shift'> & {
            values(): IterableIterator<NodeSignal<T>>;
            [Symbol.iterator](): IterableIterator<NodeSignal<T>>;
          }
      >
    : V extends AnyObject
      ? Readonly<{ [K in keyof V]: NodeSignal<V[K]> }>
      : EmptyObject);

export const NodeSignal = create(null, {
  [Symbol.hasInstance]: {
    value(this: NodeSignal, o: unknown) {
      return o != null && typeof o === 'object' && (this === o || Object.prototype.isPrototypeOf.call(this, o));
    },
  },
});

const nothing = Symbol('nothing');

export function value<V>(s: NodeSignal<V>): V;
export function value<V extends PrimitiveType>(s: NodeSignal<V>, value: V): void;
export function value<V>(s: NodeSignal<V>, v: V | typeof nothing = nothing): V {
  if (v !== nothing) {
    valueRegistry.get(s)!.set?.call(s, v);
  }

  return valueRegistry.get(s)!.get.call(s) as V;
}

export function id(signal: NodeSignal): string {
  return idRegistry.get(signal)!;
}

export const object = {
  set<SV extends AnyObject, K extends string, V>(
    nodeSignal: NodeSignal<SV>,
    key: K,
    value: V,
  ): NodeSignal<SV & Readonly<Record<K, V>>> {
    type RawSignal = Signal<Readonly<Record<keyof SV, NodeSignal<SV[keyof SV]>> & Record<K, NodeSignal<V>>>>;

    const rawSignal: RawSignal = rawSignalRegistry.get(nodeSignal)!;

    rawSignal.value = {
      ...rawSignal.value,
      [key]: signal(value),
    };

    return defineProperty(nodeSignal, key, {
      enumerable: true,
      configurable: true,
      get(this: NodeSignal) {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        return rawSignal.value[key];
      },
    });
  },
  delete<SV extends AnyObject, K extends string>(nodeSignal: NodeSignal<SV>, key: K): NodeSignal<Omit<SV, K>> {
    type RawSignal = Signal<Omit<SV, K>>;

    const rawSignal: RawSignal = rawSignalRegistry.get(nodeSignal)!;
    const { [key]: _, ...rest } = rawSignal.value;
    rawSignal.value = rest as Omit<SV, K>;

    return nodeSignal as NodeSignal<Omit<SV, K>>;
  },
};

function createPrimitiveNodeSignal<V>(primitive: V) {
  type RawSignal = Signal<V>;

  const result = create(NodeSignal);

  valueRegistry.set(result, {
    get(this: NodeSignal): V {
      const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
      return rawSignal.value;
    },
    set(this: NodeSignal, value: V) {
      const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
      rawSignal.value = value;
    },
  });

  rawSignalRegistry.set(result, createRawSignal(primitive));

  return result;
}

function createArraySignal<V extends unknown[]>(array: V) {
  type Item = V[number];
  type RawSignal = Signal<ReadonlyArray<NodeSignal<Item>>>;

  const result = create(NodeSignal, {
    push: {
      value(this: NodeSignal<V>, ...items: V): number {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        rawSignal.value = [...rawSignal.value, ...items.map((item: Item) => signal(item))];
        return rawSignal.value.length;
      },
    },
    pop: {
      value(this: NodeSignal): NodeSignal<Item> | undefined {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        const last = rawSignal.value[rawSignal.value.length - 1];
        rawSignal.value = rawSignal.value.slice(0, -1);
        return last;
      },
    },
    shift: {
      value(this: NodeSignal): NodeSignal<Item> | undefined {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        const [first] = rawSignal.value;
        rawSignal.value = rawSignal.value.slice(1);
        return first;
      },
    },
    unshift: {
      value(this: NodeSignal, ...items: V): number {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        rawSignal.value = [...items.map((item: Item) => signal(item)), ...rawSignal.value];
        return rawSignal.value.length;
      },
    },
    values: {
      value(this: NodeSignal): IterableIterator<NodeSignal<Item>> {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        return rawSignal.value.values();
      },
    },
    [Symbol.iterator]: {
      value(this: NodeSignal): IterableIterator<NodeSignal<Item>> {
        const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
        return rawSignal.value.values();
      },
    },
  });

  rawSignalRegistry.set(result, createRawSignal(array.map((item: Item) => signal(item))));

  valueRegistry.set(result, {
    get(this: NodeSignal): V {
      const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
      return rawSignal.value.map((item: NodeSignal<Item>) => valueRegistry.get(item)!.get.call(item)) as V;
    },
  });

  return result;
}

function createObjectSignal<V extends AnyObject>(value: V) {
  type RawSignal = Signal<Readonly<Record<keyof V, NodeSignal<V[keyof V]>>>>;

  const result = create(
    NodeSignal,
    fromEntries(
      keys(value).map((key) => [
        key,
        {
          enumerable: true,
          configurable: true,
          get(this: NodeSignal) {
            const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
            return rawSignal.value[key];
          },
        },
      ]),
    ) as PropertyDescriptorMap<V>,
  );

  rawSignalRegistry.set(result, createRawSignal(fromEntries(entries(value).map(([key, item]) => [key, signal(item)]))));

  valueRegistry.set(result, {
    get(this: NodeSignal): V {
      const rawSignal: RawSignal = rawSignalRegistry.get(this)!;
      return fromEntries(
        entries(rawSignal.value).map(([key, item]) => [key, valueRegistry.get(item)!.get.call(item)]),
      ) as V;
    },
  });

  return result;
}

export function signal<V>(value: V): NodeSignal<V> {
  if (isArray(value)) {
    return createArraySignal(value);
  }

  if (!!value && typeof value === 'object') {
    return createObjectSignal(value);
  }

  return createPrimitiveNodeSignal(value);
}
