import { effect, signal, Signal } from "@preact/signals-react";
import type { ConnectClient, Subscription } from "@vaadin/hilla-frontend";
import { DependencyTrackSignal, SharedSignal } from "./ExtendedSignals.js";
import type { StateEvent, SetEvent, SnapshotEvent } from "./types.js";

interface EventChannelDescriptor<T>  {
  sharedSignalId: string;
  subscribe(signalId: string, continueFrom?: string): Subscription<T>;
  publish(signalId: string, event: T): Promise<void>;
}

class EventChannel<S extends Signal = Signal> {
  private readonly channelDescriptor: EventChannelDescriptor<string>;

  private readonly connectClient: ConnectClient;
  private readonly fluxConnectionActive = signal(true);
  private channelSubscription?: Subscription<string>;

  private internalSignal: DependencyTrackSignal | null = null;
  private externalSignal: Signal | null = null;

  private readonly subscribeCount = signal(0);

  private fluxStateChangeListener = (event: CustomEvent<{active: boolean}>) => {
    this.fluxConnectionActive.value = event.detail.active
  };

  constructor(channelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient) {
    this.channelDescriptor = channelDescriptor;
    this.connectClient = connectClient;

    this.internalSignal = this.createInternalSignal();
    this.externalSignal = this.createExternalSignal(this.internalSignal);

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

    this.channelSubscription = this.channelDescriptor.subscribe(this.channelDescriptor.sharedSignalId).onNext(json => {
      const event = JSON.parse(json) as SnapshotEvent;

      const accepted = true; // TODO: evaluate the conditions against the current value and the received event
      if (accepted) {
        // Update signals based on the new value from the event:
        this.updateSignals(event);
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

  private updateSignals(event: SnapshotEvent): void {
    if (this.internalSignal) {
      if (event.value) {
        this.internalSignal.value = event.value;
      } else {
        this.internalSignal = null;
        this.externalSignal = null;
      }
    } else if (event.value) {
      this.internalSignal = this.createInternalSignal(event.value);
      this.externalSignal = this.createExternalSignal(this.internalSignal);
    }
  }

  public publish(event: StateEvent): Promise<boolean> {
    return this.channelDescriptor.publish(this.channelDescriptor.sharedSignalId, JSON.stringify(event))
          .then((_) => { return true; })
          .catch((error) => { throw Error(error); });
  }

  getSignal(): S {
    return this.externalSignal as S;
  }

  private createInternalSignal<T>(initialValue?: T): DependencyTrackSignal<T> {
    return new DependencyTrackSignal<T>(initialValue, () => this.subscribe());
  }

  private createExternalSignal<T>(internalSignal: Signal): Signal {
    return new NumberSignal(internalSignal, this);
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
    const id = crypto.randomUUID();
    const event: SetEvent = { id, set: 'id', value };
    this.eventChannel.publish(event).then(r => undefined );
  }

  compareAndSet(expectedValue: T, newValue: T): Promise<boolean> {
    const id = crypto.randomUUID();
    const event: SetEvent = {
      id,
      set: 'id',
      value: newValue,
      conditions: [{ id: 'id', value: expectedValue }],
    };

    return this.eventChannel.publish(event);
  }

  async update(updater: (value: T) => T): Promise<void> {
    while (!(await this.compareAndSet(this.value, updater(this.value)))) {}
  }
}

export class NumberSignal extends ValueSignal<number> {



  constructor(internalSignal: Signal, eventChannel: EventChannel) {
    super(internalSignal, eventChannel);
  }

  async increment(delta?: number) {
    const step = delta ?? 1;
    await this.compareAndSet(this.value, this.value + step);
  }
}

export class NumberSignalChannel extends EventChannel<NumberSignal> {
  constructor(eventChannelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient) {
    super(eventChannelDescriptor, connectClient);
  }
}
