import { computed, effect, signal, type ReadonlySignal } from './core.js';
import { createInsertLastStateEvent, createRemoveStateEvent, type StateEvent } from './events.js';
import { $processServerResponse, $update, FullStackSignal, type ServerConnectionConfig } from './FullStackSignal.js';
import { ValueSignal } from './ValueSignal.js';

export type EntryId = string;
export type Entry<T> = {
  id: EntryId;
  value: ValueSignal<T>;
  next?: EntryId;
  prev?: EntryId;
};

export class ListSignal<T> extends FullStackSignal<T> {
  #head: EntryId | undefined = undefined;
  #tail: EntryId | undefined = undefined;

  readonly #values = new Map<string, Entry<T>>();
  items: ReadonlySignal<ReadonlyArray<ValueSignal<T>>> = computed(() => []);

  // eslint-disable-next-line no-unused-private-class-members
  #size: number = 0;
  readonly #counter = signal(0);

  constructor(config: ServerConnectionConfig) {
    super(undefined, config);
    effect(() => {
      this.items = computed(() => this.#computeItems());
      this.#size = this.#counter.value;
    });
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

  protected override [$processServerResponse](event: StateEvent<T>): void {
    if (!event.accepted) {
      return;
    }
    if (event.type === 'insert' && event.position === 'last') {
      this.#handleInsertLastUpdate(event);
    } else if (event.type === 'remove') {
      this.#handleRemoveUpdate(event);
    }
  }

  #handleInsertLastUpdate(event: StateEvent<T>): void {
    if (!event.accepted) {
      return;
    }
    const valueSignal = new ValueSignal<T>(event.value as T, this.server.config);
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
    this.#counter.value += 1;
  }

  #handleRemoveUpdate(event: StateEvent<T>): void {
    const entryToRemove = this.#values.get(event.id);
    if (entryToRemove === undefined) {
      return;
    }
    this.#values.delete(event.id);
    if (this.#head === entryToRemove.id) {
      if (entryToRemove.next === undefined) {
        this.#head = undefined;
        this.#tail = undefined;
        return;
      }
      const newHead = this.#values.get(entryToRemove.next)!;
      this.#head = newHead.id;
      newHead.prev = undefined;
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
