import { CollectionSignal } from './CollectionSignal.js';
import {
  createInsertLastStateEvent,
  createRemoveStateEvent,
  type InsertLastStateEvent,
  isInsertLastStateEvent,
  isListSnapshotStateEvent,
  isRemoveStateEvent,
  type ListSnapshotStateEvent,
  type RemoveStateEvent,
  type StateEvent,
} from './events.js';
import {
  $processServerResponse,
  $resolveOperation,
  $setValueQuietly,
  $update,
  type Operation,
  type ServerConnectionConfig,
} from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

type EntryId = string;
type Entry<T> = {
  id: EntryId;
  value: ValueSignal<T>;
  next?: EntryId;
  prev?: EntryId;
};

/**
 * A {@link FullStackSignal} that represents a shared list of values, where each
 * value is represented by a {@link ValueSignal}.
 * The list can be modified by calling the defined methods to insert or remove
 * items, but the `value` property of a `ListSignal` instance is read-only and
 * cannot be assigned directly.
 * The value of each item in the list can be manipulated similar to a regular
 * {@link ValueSignal}.
 *
 * @typeParam T - The type of the values in the list.
 */
export class ListSignal<T> extends CollectionSignal<ReadonlyArray<ValueSignal<T>>> {
  #head?: EntryId;
  #tail?: EntryId;

  readonly #values = new Map<string, Entry<T>>();

  constructor(config: ServerConnectionConfig) {
    const initialValue: Array<ValueSignal<T>> = [];
    super(initialValue, config);
  }

  #computeItems(): ReadonlyArray<ValueSignal<T>> {
    let current = this.#head;
    const result: Array<ValueSignal<T>> = [];
    while (current !== undefined) {
      const entry = this.#values.get(current)!;
      result.push(entry.value);
      current = entry.next;
    }
    return result;
  }

  protected override [$processServerResponse](event: StateEvent): void {
    if (!event.accepted) {
      this[$resolveOperation](event.id, 'server rejected the operation');
      return;
    }
    if (isListSnapshotStateEvent<T>(event)) {
      this.#handleSnapshotEvent(event);
    } else if (isInsertLastStateEvent<T>(event)) {
      this.#handleInsertLastUpdate(event);
    } else if (isRemoveStateEvent(event)) {
      this.#handleRemoveUpdate(event);
    }
    this[$resolveOperation](event.id);
  }

  #handleInsertLastUpdate(event: InsertLastStateEvent<T>): void {
    if (event.entryId === undefined) {
      throw new Error('Unexpected state: Entry id should be defined when insert last event is accepted');
    }
    const valueSignal = new ValueSignal<T>(
      event.value,
      { ...this.server.config, parentClientSignalId: this.id },
      event.entryId,
    );
    const newEntry: Entry<T> = { id: valueSignal.id, value: valueSignal };

    if (this.#head === undefined) {
      this.#head = newEntry.id;
      this.#tail = this.#head;
    } else {
      const tailEntry = this.#values.get(this.#tail!)!;
      tailEntry.next = newEntry.id;
      newEntry.prev = this.#tail;
      this.#tail = newEntry.id;
    }
    this.#values.set(valueSignal.id, newEntry);
    this[$setValueQuietly](this.#computeItems());
  }

  #handleRemoveUpdate(event: RemoveStateEvent): void {
    const entryToRemove = this.#values.get(event.entryId);
    if (entryToRemove === undefined) {
      return;
    }
    this.#values.delete(event.id);
    if (this.#head === entryToRemove.id) {
      if (entryToRemove.next === undefined) {
        this.#head = undefined;
        this.#tail = undefined;
      } else {
        const newHead = this.#values.get(entryToRemove.next)!;
        this.#head = newHead.id;
        newHead.prev = undefined;
      }
    } else {
      const prevEntry = this.#values.get(entryToRemove.prev!)!;
      const nextEntry = entryToRemove.next !== undefined ? this.#values.get(entryToRemove.next) : undefined;
      if (nextEntry === undefined) {
        this.#tail = prevEntry.id;
        prevEntry.next = undefined;
      } else {
        prevEntry.next = nextEntry.id;
        nextEntry.prev = prevEntry.id;
      }
    }
    this[$setValueQuietly](this.#computeItems());
  }

  #handleSnapshotEvent(event: ListSnapshotStateEvent<T>): void {
    event.entries.forEach((entry) => {
      this.#values.set(entry.id, {
        id: entry.id,
        prev: entry.prev,
        next: entry.next,
        value: new ValueSignal(entry.value, { ...this.server.config, parentClientSignalId: this.id }, entry.id),
      });
      if (entry.prev === undefined) {
        this.#head = entry.id;
      }
      if (entry.next === undefined) {
        this.#tail = entry.id;
      }
    });
    this[$setValueQuietly](this.#computeItems());
  }

  /**
   * Inserts a new value at the end of the list.
   * @param value - The value to insert.
   */
  insertLast(value: T): Operation {
    const event = createInsertLastStateEvent(value);
    const promise = this[$update](event);
    return this.createOperation({ id: event.id, promise });
  }

  /**
   * Removes the given item from the list.
   * @param item - The item to remove.
   */
  remove(item: ValueSignal<T>): Operation {
    const entryToRemove = this.#values.get(item.id);
    if (entryToRemove === undefined) {
      return { result: Promise.resolve() };
    }
    const removeEvent = createRemoveStateEvent(entryToRemove.value.id);
    const promise = this[$update](removeEvent);
    return this.createOperation({ id: removeEvent.id, promise });
  }
}
