import { Signal } from "@preact/signals-react";
import type { ConnectClient, Subscription } from "@vaadin/hilla-frontend";
import { NumberSignal } from "./Signals.js";
import type { StateEvent, SetEvent, SnapshotEvent } from "./types.js";
import SignalsHandler from "./handler/SignalsHandler";

type EventChannelDescriptor<T> = {
  signalProviderEndpointMethod: string;
  subscribe: (channelDescriptor: string, clientSignalId: string) => Subscription<T>;
  publish: (clientSignalId: string, event: T) => Promise<void>;
};

class EventChannel<S extends Signal = Signal> {
  private readonly channelDescriptor: EventChannelDescriptor<string>;
  private readonly signalsHandler: SignalsHandler;
  private readonly id: string;

  private internalSignal: Signal | null = null;
  private externalSignal: Signal | null = null;

  constructor(signalProviderServiceMethod: string, connectClient: ConnectClient) {
    this.id = crypto.randomUUID();
    this.signalsHandler = new SignalsHandler(connectClient);
    this.channelDescriptor = {
      signalProviderEndpointMethod: signalProviderServiceMethod,
      subscribe: (signalProviderEndpointMethod: string, signalId: string) => this.signalsHandler.subscribe(signalProviderEndpointMethod, signalId),
      publish: (signalId: string, event: string) => this.signalsHandler.update(signalId, event),
    }

    this.internalSignal = this.createInternalSignal();
    this.externalSignal = this.createExternalSignal(this.internalSignal, (event: StateEvent) => this.publish(event));

    this.connect();
  }

  private connect() {
    this.channelDescriptor.subscribe(this.channelDescriptor.signalProviderEndpointMethod, this.id).onNext(json => {
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
    return this.channelDescriptor.publish(this.id, JSON.stringify(event))
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
  constructor(signalProviderEndpointMethod: string, connectClient: ConnectClient) {
    super(signalProviderEndpointMethod, connectClient);
  }
}
