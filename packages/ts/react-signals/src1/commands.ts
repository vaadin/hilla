import type { EmptyObject } from 'type-fest';
import type { ListPosition } from './ListPosition.js';
import { createId } from './utils.js';

const $brand = Symbol('SignalCommand');

/**
 * A command triggered from a signal.
 */
export type SignalCommand<T extends string = string, EX extends object = EmptyObject> = Readonly<
  {
    brand: typeof $brand;
    id: number;
    type: T;
  } & EX
>;

export interface CommandProducer<
  T extends string = string,
  A extends readonly unknown[] = [],
  P extends object = EmptyObject,
> {
  (...args: A): SignalCommand<T, P>;
  readonly name: T;
  [Symbol.hasInstance](o: unknown): o is SignalCommand<T, P>;
}

function createCommandProducer<T extends string, A extends readonly unknown[], P extends object = EmptyObject>(
  type: T,
  maker: (...args: A) => P,
): CommandProducer<T, A, P> {
  return Object.assign(
    (...args: A) => ({
      id: createId(),
      brand: $brand,
      type,
      ...maker(...args),
    }),
    {
      name: type,
      [Symbol.hasInstance](o: unknown) {
        return (
          o != null &&
          typeof o === 'object' &&
          (o as SignalCommand).brand === $brand &&
          (o as SignalCommand).type === type
        );
      },
    },
  ) as CommandProducer<T, A, P>;
}

export const Command = {
  VALUE_CONDITION: createCommandProducer('value', (expectedValue: unknown) => ({
    expectedValue,
  })),
  POSITION_CONDITION: createCommandProducer('pos', (childId: number, position: ListPosition) => ({
    childId,
    position,
  })),
  KEY_CONDITION: createCommandProducer('key', (key: string, expectedChildId: number) => ({
    key,
    expectedChildId,
  })),
  LAST_UPDATE_CONDITION: createCommandProducer('last', (expectedLastUpdate: number) => ({
    expectedLastUpdate,
  })),
  ADOPT_AS: createCommandProducer('as', (childId: number, key: string) => ({
    childId,
    key,
  })),
  ADOPT_AT: createCommandProducer('at', (childId: number, position: ListPosition) => ({
    childId,
    position,
  })),
  INCREMENT: createCommandProducer('inc', (delta: bigint | number) => ({
    delta,
  })),
  CLEAR: createCommandProducer('clear', () => ({})),
  REMOVE_BY_KEY: createCommandProducer('removeKey', (key: string) => ({
    key,
  })),
  PUT: createCommandProducer('put', (key: string, value: unknown) => ({
    key,
    value,
  })),
  PUT_IF_ABSENT: createCommandProducer('putAbsent', (key: string, value: unknown) => ({
    key,
    value,
  })),
  INSERT: createCommandProducer('insert', (value: unknown, position: ListPosition) => ({
    value,
    position,
  })),
  SET: createCommandProducer('set', (value: unknown) => ({
    value,
  })),
  REMOVE: createCommandProducer('remove', (expectedParentId?: number) => ({
    expectedParentId,
  })),
  TRANSACTION: createCommandProducer('tx', (commands: readonly SignalCommand[]) => ({
    commands,
  })),
  // SNAPSHOT: createCommandProducer('snapshot', (nodes: Map<number, unknown>) => ({
  //   nodes,
  // })),
} as const;
export type Command = (typeof Command)[keyof typeof Command];

export type CommandShape = Readonly<{
  [K in keyof typeof Command]: ReturnType<(typeof Command)[K]>;
}>;
