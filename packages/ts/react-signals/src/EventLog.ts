import {batch, effect, signal, Signal} from "@preact/signals-react";
import type {ConnectClient, Subscription} from "@vaadin/hilla-frontend";
import { DependencyTrackSignal, SharedSignal } from "./ExtendedSignals.js";
import type {SignalOptions, StateEvent, Entries, Entry, EntryId, SetEvent} from "./types.js";
import { EntryType, defaultOptions } from "./types.js";
import {DerivedState, State} from "./State.js";

interface EventQueueDescriptor<T>  {
  id: string;
  subscribe(signalId: string, lastId?: string): Subscription<T>;
  publish(signalId: string, event: T): Promise<void>;
}

const rootKey = 'ffffffff-ffff-ffff-ffff-ffffffffffff';

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
    if (rootType == EntryType.VALUE || rootType == EntryType.NUMBER) {
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
      case EntryType.NUMBER: {
        return new NumberSignal(key, internalSignal, this);
      }
      default: {
        throw new Error("Unsupported entry type: " + type);
      }
    }
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
