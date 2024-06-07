import { Signal } from "@preact/signals-react";
import type { ConnectClient, Subscription } from "@vaadin/hilla-frontend";
import { NumberSignal } from "./Signals.js";
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
    this.externalSignal = this.createExternalSignal(this.internalSignal, (event: StateEvent) => this.publish(event));

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

  private updateSignals(snapshotEvent: SnapshotEvent): void {
    if (this.internalSignal) {
      if (snapshotEvent.value) {
        this.internalSignal.value = snapshotEvent.value;
      } else {
        this.internalSignal = null;
        this.externalSignal = null;
      }
    } else if (snapshotEvent.value) {
      this.internalSignal = this.createInternalSignal(snapshotEvent.value);
      this.externalSignal = this.createExternalSignal(this.internalSignal, (event: StateEvent) => this.publish(event));
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

  private createInternalSignal(initialValue?: number): Signal<number> {
    return new Signal<number>(initialValue);
  }

  private createExternalSignal(internalSignal: Signal<number>, publish: (event: StateEvent) => Promise<boolean>): Signal<number> {
    return new NumberSignal(internalSignal, publish);
  }
}

export class NumberSignalChannel extends EventChannel<NumberSignal> {
  constructor(eventChannelDescriptor: EventChannelDescriptor<string>, connectClient: ConnectClient) {
    super(eventChannelDescriptor, connectClient);
  }
}
