import { nanoid } from 'nanoid';
import type { Simplify } from 'type-fest';
import type { Nullable } from './FullStackSignal.js';

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
export type SnapshotStateEvent<T> = CreateStateEventType<Nullable<T>, 'snapshot'>;

export type RejectStateEvent = CreateStateEventType<never, 'reject'>;

/**
 * A state event defines a new value of the signal shared with the server. The
 *
 */
export type SetStateEvent<T> = CreateStateEventType<Nullable<T>, 'set'>;

export function createSetStateEvent<T>(value: Nullable<T>): SetStateEvent<Nullable<T>> {
  return {
    id: nanoid(),
    type: 'set',
    value,
  };
}

export type ReplaceStateEvent<T> = CreateStateEventType<Nullable<T>, 'replace', { expected: Nullable<T> }>;

export function createReplaceStateEvent<T>(expected: Nullable<T>, value: Nullable<T>): ReplaceStateEvent<Nullable<T>> {
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
export type StateEvent<T> = RejectStateEvent | ReplaceStateEvent<T> | SetStateEvent<T> | SnapshotStateEvent<T>;
