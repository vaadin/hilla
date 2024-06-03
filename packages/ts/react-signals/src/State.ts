import {
  type Entries, type Entry,
  type EntryId,
  EntryType,
  type ModifiableEntry,
  type SetEvent,
  type SnapshotEvent,
  type StateEvent
} from "./types.js";


export class State {
  readonly entries: Entries = new Map();

  evaluateBatch(events: StateEvent[]): void {
    events.forEach((event) => this.evaluate(event));
  }

  evaluate(event: StateEvent): boolean {
    const id = event.id;

    if (event.conditions) {
      for(const condition of event.conditions) {
        const entry = this.get(condition.id);
        if (!entry) {
          return false;
        }

        if (condition.value !== undefined) {
          // Poor man's deep equals
          if (JSON.stringify(entry.value) !== JSON.stringify(condition.value)) {
            return false;
          }
        }

        // TODO add conditions for prev/next entry in a list
      }
    }

    if ("entries" in event) {
      const { entries } = event as SnapshotEvent;
      for(const entry of entries) {
        const {id, ...rest} = entry;
        // XXX Clean up old values before applying new snapshot (but preserve signals that are already in use)
        // XXX assuming all children are values for now
        const existing = this.get(entry.id);
        const type : EntryType = existing?.type || EntryType.VALUE;
        this.entries.set(entry.id, {...rest, type})
      }

    } else if ("set" in event) {
      const { set, value } = event as SetEvent;

      this.update(set).value = value;
    } else {
      throw new Error("Unsupported event: " + JSON.stringify(event));
    }

    return true;
  }

  ingest(source: DerivedState) {
    for(const [key, entry] of source.entries.entries()) {
      entry ? this.entries.set(key, entry) : this.delete(key);
    }
  }

  update<T>(id: EntryId): ModifiableEntry<Readonly<T>> {
    const original = this.get(id);
    if (!original) throw Error(id);

    const copy = {...original};
    this.entries.set(id, copy);
    return copy;
  }

  delete(id: EntryId) {
    this.entries.delete(id);
  }

  get<T=any>(id: EntryId): Entry<T> | undefined {
    return this.entries.get(id);
  }
}

export class DerivedState extends State {
  parent: State;

  constructor(parent: State) {
    super();
    this.parent = parent;
  }

  override delete(id: EntryId): void {
    this.entries.set(id, undefined);
  }

  override get(id: EntryId): Entry | undefined {
    return super.get(id) || this.parent.get(id);
  }

  collectTouchedKeys(touchedKeys: Set<EntryId>) {
    for (const key of this.entries.keys()) {
      touchedKeys.add(key);
    }

    if (this.parent instanceof DerivedState) {
      this.parent.collectTouchedKeys(touchedKeys);
    }
  }

  collectDiff(oldState: DerivedState): Entries {
    const touchedKeys = new Set<EntryId>();
    this.collectTouchedKeys(touchedKeys);
    oldState.collectTouchedKeys(touchedKeys);

    const diff: Entries = new Map();
    touchedKeys.forEach((key) => {
      const oldEntry = oldState.get(key);
      const newEntry = this.get(key);
      if (oldEntry !== newEntry) {
        diff.set(key, newEntry);
      }
    });

    return diff;
  }
}
