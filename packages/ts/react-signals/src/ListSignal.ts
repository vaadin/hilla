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
import { $processServerResponse, $update, FullStackSignal, type ServerConnectionConfig } from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

export type EntryId = string;
export type Entry<T> = {
  id: EntryId;
  value: ValueSignal<T>;
  next?: EntryId;
  prev?: EntryId;
};

export class ListSignal<T> extends FullStackSignal<ReadonlyArray<ValueSignal<T>>> {
  #head: EntryId | undefined = undefined;
  #tail: EntryId | undefined = undefined;

  readonly #values = new Map<string, Entry<T>>();

  constructor(config: ServerConnectionConfig) {
    const initialValue: Array<ValueSignal<T>> = [];
    super(initialValue, config);
  }

  #computeItems(): ReadonlyArray<ValueSignal<T>> {
    let current = this.#head;
    const result: Array<ValueSignal<T>> = [];
    while (current !== undefined) {
      const entry = this.#values.get(current);
      if (entry === undefined) {
        throw new Error('Unexpected state: Entry not found');
      }
      result.push(entry.value);
      current = entry.next;
    }
    return result;
  }

  protected override [$processServerResponse](event: StateEvent): void {
    if (!event.accepted) {
      return;
    }
    if (isListSnapshotStateEvent<T>(event)) {
      this.#handleSnapshotEvent(event);
    } else if (isInsertLastStateEvent<T>(event)) {
      this.#handleInsertLastUpdate(event);
    } else if (isRemoveStateEvent(event)) {
      this.#handleRemoveUpdate(event);
    }
  }

  #handleInsertLastUpdate(event: InsertLastStateEvent<T>): void {
    if (!event.accepted) {
      return;
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
      const tailEntry = this.#values.get(this.#tail!);
      if (tailEntry === undefined) {
        throw new Error('Unexpected state: Tail entry not found');
      }
      tailEntry.next = newEntry.id;
      newEntry.prev = this.#tail;
      this.#tail = newEntry.id;
    }
    this.#values.set(valueSignal.id, newEntry);
    this.setValueLocal(this.#computeItems());
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
    this.setValueLocal(this.#computeItems());
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
    this.setValueLocal(this.#computeItems());
  }

  insertLast(value: T): void {
    const event = createInsertLastStateEvent(value);
    this[$update](event);
  }

  remove(item: ValueSignal<T>): void {
    const entryToRemove = this.#values.get(item.id);
    if (entryToRemove === undefined) {
      return;
    }
    const removeEvent = createRemoveStateEvent(entryToRemove.value.id);
    this[$update](removeEvent);
  }
}
