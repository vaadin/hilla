import { Signal } from "@preact/signals-react";
import type { ConnectClient, Subscription } from "@vaadin/hilla-frontend";
import { SharedSignal } from "./ExtendedSignals.js";
import type { StateEvent, SetEvent, SnapshotEvent } from "./types.js";

interface EventChannelDescriptor<T>  {
  sharedSignalId: string;
  subscribe(signalId: string, continueFrom?: string): Subscription<T>;
  publish(signalId: string, event: T): Promise<void>;
}

class EventChannel<S extends Signal = Signal> {
  private readonly channelDescriptor: EventChannelDescriptor<string>;

  private readonly connectClient: ConnectClient;

  private internalSignal: Signal | null = null;
  private externalSignal: Signal | null = null;

  constructor(channelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient) {
    this.channelDescriptor = channelDescriptor;
    this.connectClient = connectClient;

    this.internalSignal = this.createInternalSignal();
    this.externalSignal = this.createExternalSignal(this.internalSignal);

    this.connect();
  }

  private connect() {
    this.channelDescriptor.subscribe(this.channelDescriptor.sharedSignalId).onNext(json => {
      const event = JSON.parse(json) as SnapshotEvent;

      const accepted = true; // TODO: evaluate the conditions against the current value and the received event
      if (accepted) {
        // Update signals based on the new value from the event:
        this.updateSignals(event);
      }
    });
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

  private createInternalSignal<T>(initialValue?: T): Signal<T> {
    return new Signal<T>(initialValue);
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
