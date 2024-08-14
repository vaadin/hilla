import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import { signal, effect } from './core.js';
import { NumberSignal, setInternalValue, type ValueSignal } from './Signals.js';
import SignalsHandler from './SignalsHandler';
import { type StateEvent, StateEventType } from './types.js';

/**
 * The type that describes the needed information to
 * subscribe and publish to a server-side signal instance.
 */
type SignalChannelDescriptor<T> = Readonly<{
  signalProviderEndpointMethod: string;
  subscribe(signalProviderEndpointMethod: string, clientSignalId: string): Subscription<T>;
  publish(clientSignalId: string, event: T): Promise<void>;
}>;

/**
 * A generic class that represents a signal channel
 * that can be used to communicate with a server-side
 * signal instance.
 *
 * The signal channel is responsible for subscribing to
 * the server-side signal and updating the local signal
 * based on the received events.
 *
 * @typeParam T - The type of the signal value.
 * @typeParam S - The type of the signal instance.
 */
abstract class SignalChannel<T, S extends ValueSignal<T>> {
  readonly #channelDescriptor: SignalChannelDescriptor<StateEvent>;
  readonly #signalsHandler: SignalsHandler;
  readonly #id: string;

  readonly #internalSignal: S;

  readonly #subscribeCount = signal(0);
  #subscription?: Subscription<StateEvent>;

  #subscribe(): void {
    // Update asynchronously to avoid side effects when this is run inside compute()
    setTimeout(() => {
      this.#subscribeCount.value += 1;
    }, 0);
  }

  #unsubscribe(): void {
    // Update asynchronously to avoid side effects when this is run inside compute()
    setTimeout(() => {
      this.#subscribeCount.value -= 1;
    }, 0);
  }

  constructor(signalProviderServiceMethod: string, connectClient: ConnectClient) {
    this.#id = nanoid();
    this.#signalsHandler = new SignalsHandler(connectClient);
    this.#channelDescriptor = {
      signalProviderEndpointMethod: signalProviderServiceMethod,
      subscribe: (signalProviderEndpointMethod: string, signalId: string) =>
        this.#signalsHandler.subscribe(signalProviderEndpointMethod, signalId),
      publish: async (signalId: string, event: StateEvent) => this.#signalsHandler.update(signalId, event),
    };

    this.#internalSignal = this.createInternalSignal(
      async (event: StateEvent) => this.publish(event),
      undefined,
      () => this.#subscribe(),
      () => this.#unsubscribe(),
    );

    effect(() => {
      if (this.#subscribeCount.value > 0) {
        this.#connect();
      } else {
        this.#disconnect();
      }
    });
  }

  #connect() {
    if (this.#subscription) {
      return;
    }

    this.#subscription = this.#channelDescriptor
      .subscribe(this.#channelDescriptor.signalProviderEndpointMethod, this.#id)
      .onNext((stateEvent) => {
        // Update signals based on the new value from the event:
        this.#updateSignals(stateEvent);
      });
  }

  #disconnect() {
    if (!this.#subscription) {
      return;
    }

    this.#signalsHandler.unsubscribe(this.#id);
    this.#subscription.cancel();
    this.#subscription = undefined;
  }

  #updateSignals(stateEvent: StateEvent): void {
    if (stateEvent.type === StateEventType.SNAPSHOT) {
      setInternalValue(this.#internalSignal, stateEvent.value);
    }
  }

  async publish(event: StateEvent): Promise<boolean> {
    await this.#channelDescriptor.publish(this.#id, event);
    return true;
  }

  /**
   * Returns the signal instance to be used in components.
   */
  get signal(): S {
    return this.#internalSignal;
  }

  /**
   * Returns the id of the signal channel.
   */
  get id(): string {
    return this.#id;
  }

  abstract createInternalSignal(
    publish: (event: StateEvent) => Promise<boolean>,
    initialValue: T | undefined,
    onSubscribe: () => void,
    onUnsubscribe: () => void,
  ): S;
}

/**
 * A signal channel that is used to communicate with a
 * server-side signal instance that holds a number value.
 */
export class NumberSignalChannel extends SignalChannel<number, NumberSignal> {
  override createInternalSignal(
    publish: (event: StateEvent) => Promise<boolean>,
    initialValue: number | undefined,
    onSubscribe: () => void,
    onUnsubscribe: () => void,
  ): NumberSignal {
    return new NumberSignal(publish, initialValue, onSubscribe, onUnsubscribe);
  }
}
