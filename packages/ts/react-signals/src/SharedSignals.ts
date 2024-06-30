import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { type ReadonlySignal, Signal, batch, computed, effect, signal } from '@preact/signals-react';

const rootKey = 'ffffffff-ffff-ffff-ffff-ffffffffffff';

declare module "@preact/signals-react" {
  // https://github.com/preactjs/signals/issues/351#issuecomment-1515488634
	class Signal {
    protected S(node: any): void;
    protected U(node: any): void
  }
}

class DependencyTrackSignal<T = any> extends Signal<T> {
  private readonly onSubscribe: () => void;
  private readonly onUnsubscribe: () => void;

  private subscribeCount = 0;

  constructor(value: T | undefined, onSubscribe: () => void, onUnsubscribe: () => void) {
    super(value);
    this.onSubscribe = onSubscribe;
    this.onUnsubscribe = onUnsubscribe;
  }

  protected override S(node: any): void {
    super.S(node);
    if (this.subscribeCount++ == 0) {
      this.onSubscribe.call(null);
    }
  }

  protected override U(node: any): void {
    super.U(node);
    if (--this.subscribeCount == 0) {
      this.onUnsubscribe.call(null);
    }
  }
}

export enum EntryType {
  VALUE,
  LIST,
  NUMBER,
}

interface ModifiableEntry<T = any> {
  value: T;
  next: EntryId | null;
  prev: EntryId | null;
  type: EntryType;
}

type Entry<T = any> = Readonly<ModifiableEntry<T>>;

interface EventCondition {
  id: EntryId;
  value?: any;
  // TODO add conditions for prev / next pointers
}

interface StateEvent {
  id: string;
  conditions?: EventCondition[];
}

interface SetEvent extends StateEvent {
  set: EntryId;
  value: any;
}

interface InsertEvent extends StateEvent {
  entry: EntryId;
  direction: "BEFORE" | "AFTER";
  reference: EntryId | null;
  value: any;
}

interface RemoveEvent extends StateEvent {
  remove: string;
  parent: EntryId;
}

interface SnapshotEvent extends StateEvent {
  entries: {
    id: EntryId;
    next: EntryId | null;
    prev: EntryId | null;
    value: any;
  }[];
}

type EntryId = string;
type EntryReference<S = SharedSignal<any>> = EntryId | S;
type Entries = Map<EntryId, Entry | undefined>;

class State {
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

    } else if ("remove" in event) {
      const { remove, parent } = event as RemoveEvent;

      // XXX Verify that the entry to remove is a child of the suggested parent?

      const entry = this.get(remove);
      if (!entry) {
        console.log("Removing no-existent entry", remove);
        return false;
      }

      const parentEntry = this.get<ListRoot>(parent);
      if (!parentEntry) {
        console.log("Removing from no-existent parent", parent);
        return false;
      }

      const newListRoot = {...parentEntry.value};

      if (entry.prev) {
        this.update(entry.prev).next = entry.next;
      } else {
        newListRoot.head = entry.next;
      }

      if (entry.next) {
        this.update(entry.next).prev = entry.prev;
      } else {
        newListRoot.tail = entry.prev;
      }

      // XXX Also unlink any children
      this.delete(remove);

      // Always update the list entry to trigger a signal update
      this.update(parent).value = newListRoot;

    } else if ("direction" in event) {
      const {entry: listId, direction, reference, value} = event as InsertEvent;
      const listRoot = this.get<ListRoot>(listId);
      if (!listRoot) {
        console.log("Inserting into non-existent list");
        return false;
      }

      const { head, tail } = listRoot.value;

      let prev: EntryId | null = null;
      let next: EntryId | null = null;

      if (direction == "AFTER") {
        prev = reference || tail;
        if (prev) {
          const prevEntry = this.get(prev);
          if (!prevEntry) {
            console.log("Inserting before non-existent entry");
            return false;
          }
          next = prevEntry.next;
        }
      } else {
        next = reference || head;
        if (next) {
          const nextEntry = this.get(next);
          if (!nextEntry) {
            console.log("Inserting after non-existent entry");
            return false;
          }
          prev = nextEntry.prev;
        }
      }

      // XXX Assuming all list children are values for now
      this.insert(id, { value, next, prev, type: EntryType.VALUE });

      const newListRoot = {...listRoot.value};

      if (next) {
        this.update(next).prev = id;
      } else {
        newListRoot.tail = id;
      }

      if (prev) {
        this.update(prev).next = id;
      } else {
        newListRoot.head = id;
      }

      // Always update the list entry to trigger a signal update
      this.update(listId).value = newListRoot;

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

  insert(id: EntryId, entry : Entry) {
    if (this.get(id)) throw Error(id);
    this.entries.set(id, entry);
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

class DerivedState extends State {
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

class EventLog<R extends Signal = Signal> {
    private readonly connectClient: ConnectClient;
    private readonly queue: EventQueueDescriptor<string>;
    private readonly options: { delay: boolean; };

    private readonly subscribeCount = signal(0);
    private readonly fluxConnectionActive = signal(true);

    private readonly pendingChanges: Record<string, StateEvent> = {};
    private readonly pendingResults: Record<string, (accepted: boolean) => void> = {};

    private readonly confirmedState = new State();
    private visualState = new DerivedState(this.confirmedState);
    private readonly internalSignals: Map<EntryId, DependencyTrackSignal> = new Map();
    private readonly externalSignals: Map<EntryId, Signal> = new Map();

    private subscription?: Subscription<string>;
    private lastEvent?: string;

    private fluxStateChangeListener = (event: CustomEvent<{active: boolean}>) => {
        this.fluxConnectionActive.value = event.detail.active
    };

    constructor(queue: EventQueueDescriptor<string>, connectClient: ConnectClient, options: SignalOptions, rootType: EntryType) {
        this.queue = queue;
        this.options = {...defaultOptions, ...options};
        this.connectClient = connectClient;

        let rootValue: any;
        if (rootType == EntryType.LIST)  {
          const listRoot: ListRoot = { head: null, tail: null };
          rootValue = listRoot;
        } else if (rootType == EntryType.VALUE || rootType == EntryType.NUMBER) {
          rootValue = options.initialValue;
        } else {
          throw Error(rootType);
        }

        const internalRootSignal = this.createInternalSignal(rootValue);
        this.internalSignals.set(rootKey, internalRootSignal) ;
        this.externalSignals.set(rootKey, this.createExternalSignal(rootKey, internalRootSignal, rootType));
        this.confirmedState.entries.set(rootKey, {type: rootType, next: null, prev: null, value: rootValue});

        effect(() => {
          if (this.subscribeCount.value > 0 && this.fluxConnectionActive.value) {
            this.connect();
          } else {
            this.disconnect();
          }
        });
    }

    private subscribe(): void {
        // Update asynchronously to avoid side effects when this is run inside compute()
        setTimeout(() => this.subscribeCount.value++, 0);
      }

    private unsubscribe(): void {
        // Update asynchronously to avoid side effects when this is run inside compute()
        setTimeout(() => this.subscribeCount.value--, 0);
    }

    private connect() {
        if (this.subscription) {
            return;
        }
        console.log("Opening connection");

        this.subscription = this.queue.subscribe(this.queue.id, this.lastEvent).onNext(json => {
            const event = JSON.parse(json) as StateEvent;
            this.lastEvent = event.id;

            if (event.id in this.pendingChanges) {
                delete this.pendingChanges[event.id];
            }

            // Create as a derived state so we can diff against the old confirmed state
            const newConfirmedState = new DerivedState(this.confirmedState);
            const accepted = newConfirmedState.evaluate(event);

            if (accepted) {
                // Create a new visible state by applying the current change + pending changes against the confirmed state
                const newVisualState = new DerivedState(newConfirmedState);
                newVisualState.evaluateBatch(Object.values(this.pendingChanges));

                // Create a diff between old and new visible state
                const diff = newVisualState.collectDiff(this.visualState);

                // Update confirmed state based on the current change
                this.confirmedState.ingest(newConfirmedState);
                newVisualState.parent = this.confirmedState;

                // Set the new visible state as the official visible state
                this.visualState = newVisualState;

                // Update signals based on the diff
                this.updateSignals(diff);
            }

            if (event.id in this.pendingResults) {
                this.pendingResults[event.id](accepted);
                delete this.pendingResults[event.id];
            }
        });

        this.connectClient.fluxConnection.addEventListener('state-changed', this.fluxStateChangeListener);
    }

    private disconnect() {
        if (!this.subscription) {
            return;
        }

        console.log("Closing connection");
        this.subscription.cancel();
        this.subscription = undefined;

        this.connectClient.fluxConnection.removeEventListener('state-changed', this.fluxStateChangeListener);
    }

    private updateSignals(diff: Entries) {
        batch(() => {
            for (const [key, entry] of diff.entries()) {
              const signal = this.internalSignals.get(key);
              if (signal) {
                  if (entry) {
                      // TODO re-create external signal if entry type has changed
                      signal.value = entry.value;
                  } else {
                      signal.value = null;
                      this.internalSignals.delete(key);
                      this.externalSignals.delete(key);
                  }
              } else if (entry) {
                  const internalSignal = this.createInternalSignal(entry.value);
                  this.internalSignals.set(key, internalSignal);
                  this.externalSignals.set(key, this.createExternalSignal(key, internalSignal, entry.type));
              }
            }
        });
    }

    private addPendingChange(event: StateEvent) {
        this.pendingChanges[event.id] = event;

        const newVisualState = new DerivedState(this.visualState);
        if (newVisualState.evaluate(event)) {
            const diff = newVisualState.collectDiff(this.visualState);

            this.visualState.ingest(newVisualState);
            this.updateSignals(diff);
        }
    }

    private removePendingChange(event: StateEvent) {
        delete this.pendingChanges[event.id];

        const newVisualState = new DerivedState(this.confirmedState);
        newVisualState.evaluateBatch(Object.values(this.pendingChanges));

        const diff = newVisualState.collectDiff(this.visualState);

        this.visualState = newVisualState;

        this.updateSignals(diff);
    }

    public publish(event : StateEvent, latencyCompensate : boolean): Promise<boolean> {
        if (latencyCompensate) {
            this.addPendingChange(event);
        }
        return new Promise((resolve, reject) => {
            this.pendingResults[event.id] = resolve;

            const action = () => this.queue.publish(this.queue.id, JSON.stringify(event)).catch((error) => {
                if (latencyCompensate) {
                    this.removePendingChange(event);
                }
                reject(error);
            });
            this.options.delay ? setTimeout(action, 2000) : action();
        });
    }

    getSignal(id: string): Signal<any> | undefined {
      return this.externalSignals.get(id);
    }

    getRoot(): R {
      return this.externalSignals.get(rootKey) as R;
    }

    getEntry(key: string): Entry | undefined {
      return this.visualState.get(key);
    }

    private createInternalSignal<T>(initialValue: T): DependencyTrackSignal<T> {
      return new DependencyTrackSignal<T>(initialValue, () => this.subscribe(), () => this.unsubscribe());
    }

    private createExternalSignal<T>(key: EntryId, internalSignal: Signal, type: EntryType): Signal {
      switch(type) {
        case EntryType.LIST: {
          return new ListSignal(key, internalSignal, this);
        }
        case EntryType.VALUE: {
          return new ValueSignal(key, internalSignal, this);
        }
        case EntryType.NUMBER: {
          return new NumberSignal(key, internalSignal, this);
        }
        default: {
          throw new Error("Unsupported entry type: " + type);
        }
      }
    }
}

declare class Computed<T> extends Signal<T> implements ReadonlySignal<T> {
  constructor(compute: () => T);
}

function Computed <T>(this: Computed<T>, compute: () => T) {
  // Replica of the private Computed constructor
	Signal.call(this, undefined);

  const anyThis = this as any;
    // _compute
	anyThis.x = compute;
    // _sources
	anyThis.s = undefined;
    // _globalVersion
	anyThis.g =  - 1;
    // _flags = OUTDATED
	anyThis.f = 1 << 2;
}

// Create dummy real Computed to be able to get its prototype
Computed.prototype = (computed(() => {}) as any).__proto__;

abstract class SharedSignal<T> extends Computed<T> {
  override readonly key: EntryId;

  constructor(compute: () => T, key: EntryId) {
    super(compute);
    this.key = key;
  }
}

interface EventQueueDescriptor<T>  {
  id: string;
  subscribe(signalId: string, lastId?: string): Subscription<T>;
  publish(signalId: string, event: T): Promise<void>;
}

interface FullSignalOptions {
  delay: boolean;
  initialValue: any;
}

const defaultOptions : FullSignalOptions = {
  delay: false,
  initialValue: null,
}

type SignalOptions = Partial<FullSignalOptions>;

interface ListInsertResult<S extends SharedSignal<any>> {
  readonly promise: Promise<boolean>,

  readonly signal: S,
}

// Entry value for the root of a list
interface ListRoot {
  head: string | null,
  tail: string | null
}

export class ValueSignal<T> extends SharedSignal<T> {
  private readonly eventLog: EventLog;

  constructor(key: EntryId, internalSignal: Signal, eventLog: EventLog) {
    super(() => internalSignal.value, key);

    this.eventLog = eventLog;
  }

  override get value() {
    return super.value;
  }

  override set value(value: T) {
    this.set(value, true);
  }

  set(value: T, eager: boolean): Promise<void> {
    const id = crypto.randomUUID();
    const event: SetEvent = { id, set: this.key, value };
    return this.eventLog.publish(event, eager).then((_) => undefined);
  }

  compareAndSet(expectedValue: T, newValue: T, eager = true): Promise<boolean> {
    const id = crypto.randomUUID();
    const event: SetEvent = {
      id,
      set: this.key,
      value: newValue,
      conditions: [{ id: this.key, value: expectedValue }],
    };

    return this.eventLog.publish(event, eager);
  }

  async update(updater: (value: T) => T): Promise<void> {
    // TODO detect accessing other signals and re-run if any of those are changed as well
    // TODO conditional on last change id for the signal rather than the value itself to avoid the ABA problem
    while (!(await this.compareAndSet(this.value, updater(this.value)))) {}
  }
}

export class NumberSignal extends ValueSignal<number> {

  constructor(key: EntryId, internalSignal: Signal, eventLog: EventLog) {
    super(key, internalSignal, eventLog);
  }

  async increment(delta?: number): Promise<void> {
    delta ??= 1;
    await this.compareAndSet(this.value, this.value + delta);
  }
}

export class NumberSignalQueue extends EventLog<NumberSignal> {
  constructor(queue: EventQueueDescriptor<string>, connectClient: ConnectClient, initialValue?: number, eager: boolean = true) {
    const options: SignalOptions = {
      delay: !eager,
      initialValue: initialValue ?? 0,
    };
    super(queue, connectClient, options, EntryType.NUMBER);
  }
}

function getKey(target: EntryReference): string {
  if (typeof target == 'string') {
    return target;
  } else {
    return target.key;
  }
}

export class ListSignal<T = any, S extends ValueSignal<T> = ValueSignal<T>> extends SharedSignal<S[]> {
  private readonly eventLog: EventLog;

  constructor(key: EntryId, internalSignal: Signal, eventLog: EventLog) {
    super(() => {
      const value: S[] = [];
      const root = internalSignal.value as ListRoot;
      if (root) {
        let key = root.head;
        while (key) {
          const signal = eventLog.getSignal(key);
          if (!signal) {
            throw new Error("Should not happen?");
          }
          value.push(signal as S);

          const node = eventLog.getEntry(key);
          if (!node) {
            throw new Error("Should not happen?");
          }
          key = node.next;
        }
      }
      return value;
    }, key);

    this.eventLog = eventLog;
  }

  get(key: EntryId): S | undefined {
    // TODO: Avoid setting up a subscription
    // TODO: Find the signal by id and verify that it's a child instead of looping through children
    return this.value.find((signal) => signal.key == key);
  }

  get items(): ReadonlySignal<T[]> {
    // TODO: Maybe lazy create and cache these computed signals?
    return computed(() => this.value.map(({value}) => value));
  }

  get entries(): ReadonlySignal<[T, EntryId][]> {
    // TODO: Maybe lazy create and cache these computed signals?
    return computed(() => this.value.map(({key, value}) => [value, key]));
  }

  insertLast(value: T): ListInsertResult<S> {
    const id = crypto.randomUUID();
    const event: InsertEvent = { entry: this.key, id, direction: "AFTER", reference: null, value };
    return {promise: this.eventLog.publish(event, true), signal: this.get(id)!};
  }

  insertFirst(value: T): ListInsertResult<S> {
    const id = crypto.randomUUID();
    const event: InsertEvent = { entry: this.key, id, direction: "BEFORE", reference: null, value };
    return {promise: this.eventLog.publish(event, true), signal: this.get(id)!};
  }

  insertBefore(reference: EntryReference<S>, value: T): ListInsertResult<S> {
    const id = crypto.randomUUID();
    const event: InsertEvent = { entry: this.key, id, direction: "BEFORE", reference: getKey(reference), value };
    return {promise: this.eventLog.publish(event, true), signal: this.get(id)!};
  }

  insertAfter(reference: EntryReference<S>, value: T): ListInsertResult<S> {
    const id = crypto.randomUUID();
    const event: InsertEvent = { entry: this.key, id, direction: "AFTER", reference: getKey(reference), value };
    return {promise: this.eventLog.publish(event, true), signal: this.get(id)!};
  }

  remove(child: EntryReference<S>) {
    const id = crypto.randomUUID();
    const event: RemoveEvent = { id, remove: getKey(child), parent: this.key};
    this.eventLog.publish(event, true);
  }
}

export { EventLog, type SignalOptions, type ListInsertResult };
