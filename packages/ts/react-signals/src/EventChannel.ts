import {batch, effect, signal, Signal} from "@preact/signals-react";
import type {ConnectClient, Subscription} from "@vaadin/hilla-frontend";
import { DependencyTrackSignal, SharedSignal } from "./ExtendedSignals.js";
import type {SignalOptions, StateEvent, Entries, Entry, EntryId, SetEvent} from "./types.js";
import { EntryType, defaultOptions } from "./types.js";
import {DerivedState, State} from "./State.js";

interface EventChannelDescriptor<T>  {
  sharedSignalId: string;
  subscribe(signalId: string, continueFrom?: string): Subscription<T>;
  publish(signalId: string, event: T): Promise<void>;
}

class EventChannel<S extends Signal = Signal> {
  private readonly channelDescriptor: EventChannelDescriptor<string>;
  private readonly options: { delay?: number; };

  private readonly connectClient: ConnectClient;
  private readonly fluxConnectionActive = signal(true);
  private channelSubscription?: Subscription<string>;

  private readonly pendingChanges: Record<string, StateEvent> = {};
  private readonly pendingResults: Record<string, (accepted: boolean) => void> = {};

  private internalSignal: DependencyTrackSignal | null = null;
  private externalSignal: Signal | null = null;

  private readonly confirmedState = new State();
  private visualState = new DerivedState(this.confirmedState);
  private continueFrom?: string;

  private readonly subscribeCount = signal(0);

  private fluxStateChangeListener = (event: CustomEvent<{active: boolean}>) => {
    this.fluxConnectionActive.value = event.detail.active
  };

  constructor(channelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient, options: SignalOptions, valueType: EntryType) {
    this.channelDescriptor = channelDescriptor;
    this.options = {...defaultOptions, ...options};
    this.connectClient = connectClient;

    let defaultValue: unknown;
    if (valueType == EntryType.VALUE || valueType == EntryType.NUMBER) {
      defaultValue = options.initialValue;
    } else {
      throw Error(valueType);
    }

    this.internalSignal = this.createInternalSignal(defaultValue);
    this.externalSignal = this.createExternalSignal(this.internalSignal, valueType);

    this.confirmedState.entries.set('id', {type: valueType, value: defaultValue});

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
    if (this.channelSubscription) {
      return;
    }

    this.channelSubscription = this.channelDescriptor.subscribe(this.channelDescriptor.sharedSignalId, this.continueFrom).onNext(json => {
      const event = JSON.parse(json) as StateEvent;
      this.continueFrom = event.id;

      if (event.id in this.pendingChanges) {
        delete this.pendingChanges[event.id];
      }

      // Create as a derived state, so we can diff against the old confirmed state
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
    if (!this.channelSubscription) {
      return;
    }

    this.channelSubscription.cancel();
    this.channelSubscription = undefined;

    this.connectClient.fluxConnection.removeEventListener('state-changed', this.fluxStateChangeListener);
  }

  private updateSignals(diff: Entries) {
    batch(() => {
      for (const [key, entry] of diff.entries()) {
        if (this.internalSignal) {
          if (entry) {
            this.internalSignal.value = entry.value;
          } else {
            this.internalSignal = null;
            this.externalSignal = null;
          }
        } else if (entry) {
          this.internalSignal = this.createInternalSignal(entry.value);
          this.externalSignal = this.createExternalSignal(this.internalSignal, entry.type);
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

  public publish(event: StateEvent, latencyCompensate: boolean): Promise<boolean> {
    if (latencyCompensate) {
      this.addPendingChange(event);
    }
    return new Promise((resolve, reject) => {
      this.pendingResults[event.id] = resolve;

      const action = () => this.channelDescriptor.publish(this.channelDescriptor.sharedSignalId, JSON.stringify(event)).catch((error) => {
        if (latencyCompensate) {
          this.removePendingChange(event);
        }
        reject(error);
      });
      this.options.delay ? setTimeout(action, this.options.delay) : action();
    });
  }

  getSignal(): S {
    return this.externalSignal as S;
  }

  private createInternalSignal<T>(initialValue: T): DependencyTrackSignal<T> {
    return new DependencyTrackSignal<T>(initialValue, () => this.subscribe());
  }

  private createExternalSignal<T>(internalSignal: Signal, type: EntryType): Signal {
    switch(type) {
      case EntryType.NUMBER: {
        return new NumberSignal(internalSignal, this);
      }
      default: {
        throw new Error("Unsupported entry type: " + type);
      }
    }
  }
}

export class ValueSignal<T> extends SharedSignal<T> {
  private readonly eventChannel: EventChannel;

  constructor(internalSignal: Signal, eventChannel: EventChannel) {
    super(() => internalSignal.value);

    this.eventChannel = eventChannel;
  }

  override get value() {
    return super.value;
  }

  override set value(value: T) {
    this.set(value, true);
  }

  set(value: T, latencyCompensate: boolean): Promise<void> {
    const id = crypto.randomUUID();
    const event: SetEvent = { id, set: 'id', value };
    return this.eventChannel.publish(event, latencyCompensate).then((_) => undefined);
  }

  compareAndSet(expectedValue: T, newValue: T, latencyCompensate: boolean = true): Promise<boolean> {
    const id = crypto.randomUUID();
    const event: SetEvent = {
      id,
      set: 'id',
      value: newValue,
      conditions: [{ id: 'id', value: expectedValue }],
    };

    return this.eventChannel.publish(event, latencyCompensate);
  }

  async update(updater: (value: T) => T): Promise<void> {
    while (!(await this.compareAndSet(this.value, updater(this.value)))) {}
  }
}

export class NumberSignal extends ValueSignal<number> {

  constructor(internalSignal: Signal, eventChannel: EventChannel) {
    super(internalSignal, eventChannel);
  }

  async increment(delta?: number): Promise<void> {
    const step = delta ?? 1;
    await this.compareAndSet(this.value, this.value + step);
  }
}

export class NumberSignalChannel extends EventChannel<NumberSignal> {
  constructor(eventChannelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient, initialValue?: number, delay?: number) {
    const options: SignalOptions = {
      delay,
      initialValue,
    };
    super(eventChannelDescriptor, connectClient, options, EntryType.NUMBER);
  }
}
