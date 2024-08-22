import { nanoid } from 'nanoid';
import type { Simplify } from 'type-fest';

/**
 * Creates a new state event type.
 */
type CreateStateEventType<V, T extends string, C extends Record<string, unknown> = Record<never, never>> = Simplify<
  Readonly<{
    id: string;
    type: T;
    value: V;
  }> &
    Readonly<C>
>;

/**
 * A state event received from the server describing the current state of the
 * signal.
 */
export type SnapshotStateEvent<T> = CreateStateEventType<T, 'snapshot'>;

/**
 * A state event defines a new value of the signal shared with the server. The
 *
 */
export type SetStateEvent<T> = CreateStateEventType<T, 'set'>;

export function createSetStateEvent<T>(value: T): SetStateEvent<T> {
  return {
    id: nanoid(),
    type: 'set',
    value,
  };
}

export type ReplaceStateEvent<T> = CreateStateEventType<T, 'replace', { expected: T }>;

export function createReplaceStateEvent<T>(expected: T, value: T): ReplaceStateEvent<T> {
  return {
    id: nanoid(),
    type: 'replace',
    value,
    expected,
  };
}

/**
 * An object that describes the change of the signal state.
 */
export type StateEvent<T> = ReplaceStateEvent<T> | SetStateEvent<T> | SnapshotStateEvent<T>;
