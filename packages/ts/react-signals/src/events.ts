import { nanoid } from 'nanoid';

export type StateEvent = Readonly<{
  id: string;
  type: string;
  value: unknown;
  accepted: boolean;
}>;

/**
 * Creates a new state event type.
 */
type CreateStateEventType<V, T extends string, C extends Record<string, unknown> = Record<never, never>> = Readonly<{
  type: T;
  value: V;
}> &
  Readonly<C> &
  StateEvent;

/**
 * A state event received from the server describing the current state of the
 * signal.
 */
export type SnapshotStateEvent<T> = CreateStateEventType<T, 'snapshot'>;

/**
 * A state event defines a new value of the signal shared with the server. The
 */
export type SetStateEvent<T> = CreateStateEventType<T, 'set'>;

export function createSetStateEvent<T>(value: T): SetStateEvent<T> {
  return {
    id: nanoid(),
    type: 'set',
    value,
    accepted: false,
  };
}

export type ReplaceStateEvent<T> = CreateStateEventType<T, 'replace', { expected: T }>;

export function createReplaceStateEvent<T>(expected: T, value: T): ReplaceStateEvent<T> {
  return {
    id: nanoid(),
    type: 'replace',
    value,
    expected,
    accepted: false,
  };
}

export type IncrementStateEvent = CreateStateEventType<number, 'increment'>;

export function createIncrementStateEvent(delta: number): IncrementStateEvent {
  return {
    id: nanoid(),
    type: 'increment',
    value: delta,
    accepted: false,
  };
}

export type ListEntry<T> = Readonly<{
  id: string;
  prev?: string;
  next?: string;
  value: T;
}>;

export type ListSnapshotStateEvent<T> = CreateStateEventType<never, 'snapshot', { entries: Array<ListEntry<T>> }>;

export type InsertLastStateEvent<T> = CreateStateEventType<T, 'insert', { position: 'last' }>;

export function createInsertLastStateEvent<T>(value: T): InsertLastStateEvent<T> {
  return {
    id: nanoid(),
    type: 'insert',
    value,
    position: 'last',
    accepted: false,
  };
}

export type RemoveStateEvent = CreateStateEventType<never, 'remove', { entryId: string }>;

export function createRemoveStateEvent(entryId: string): RemoveStateEvent {
  return {
    id: nanoid(),
    type: 'remove',
    entryId,
    value: undefined as never,
    accepted: false,
  };
}

function isStateEvent(event: unknown): event is StateEvent {
  return (
    typeof event === 'object' &&
    event !== null &&
    typeof (event as { id?: unknown }).id === 'string' &&
    typeof (event as { type?: unknown }).type === 'string' &&
    typeof (event as { value?: unknown }).value !== 'undefined' &&
    typeof (event as { accepted?: unknown }).accepted === 'boolean'
  );
}

export function isSnapshotStateEvent<T>(event: unknown): event is SnapshotStateEvent<T> {
  return isStateEvent(event) && event.type === 'snapshot';
}

export function isSetStateEvent<T>(event: unknown): event is SetStateEvent<T> {
  return isStateEvent(event) && event.type === 'set';
}

export function isReplaceStateEvent<T>(event: unknown): event is ReplaceStateEvent<T> {
  return (
    isStateEvent(event) && typeof (event as { expected?: unknown }).expected !== 'undefined' && event.type === 'replace'
  );
}

export function isIncrementStateEvent(event: unknown): event is IncrementStateEvent {
  return isStateEvent(event) && event.type === 'increment';
}

export function isListSnapshotStateEvent<T>(event: unknown): event is ListSnapshotStateEvent<T> {
  return (
    typeof event === 'object' &&
    event !== null &&
    typeof (event as { id?: unknown }).id === 'string' &&
    (event as { type?: unknown }).type === 'snapshot' &&
    (event as { entries?: unknown }).entries instanceof Array
  );
}

export function isInsertLastStateEvent<T>(event: unknown): event is InsertLastStateEvent<T> {
  return isStateEvent(event) && event.type === 'insert' && (event as { position?: unknown }).position === 'last';
}

export function isRemoveStateEvent(event: unknown): event is RemoveStateEvent {
  return isStateEvent(event) && event.type === 'remove';
}
