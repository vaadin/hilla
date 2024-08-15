import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import { effect } from './core.js';
import type { ValueSignal } from './Signals.js';

const ENDPOINT = 'SignalsHandler';

/**
 * Types of changes that can be produced or processed by a signal.
 */
export enum StateEventType {
  SET = 'set',
  SNAPSHOT = 'snapshot',
}

/**
 * An object that describes the change of the signal state.
 */
export type StateEvent = Readonly<{
  id: string;
  type: StateEventType;
  value: unknown;
}>;

/**
 * A signal channel that can be used to communicate with a server-side.
 *
 * The signal channel is responsible for subscribing to the server-side signal
 * and updating the local signal based on the received events.
 *
 * @typeParam S - The type of the signal instance.
 */
export class SignalChannel {
  readonly #id = nanoid();
  readonly #client: ConnectClient;
  readonly #method: string;
  #subscription?: Subscription<StateEvent>;

  /**
   * @param client - The client instance to be used for communication.
   * @param signalProviderEndpointMethod - The method name of the signal provider
   * service.
   * @returns The signal channel instance.
   */
  constructor(client: ConnectClient, signalProviderEndpointMethod: string) {
    this.#client = client;
    this.#method = signalProviderEndpointMethod;
  }

  get id(): string {
    return this.#id;
  }

  cancel(): void {
    this.#subscription?.cancel();
    this.#subscription = undefined;
  }

  /**
   * Connects the local signal to the server.
   *
   * @param signal - The signal instance to be connected.
   * @param onUpdate - The callback that will be called when the signal is
   * updated.
   */
  connect(signal: ValueSignal, onUpdate: (promise: Promise<void>) => void): void {
    let paused = false;
    this.#subscription ??= this.#client
      .subscribe(ENDPOINT, 'subscribe', {
        signalProviderEndpointMethod: this.#method,
        clientSignalId: this.#id,
      })
      .onNext((event: StateEvent) => {
        if (event.type === StateEventType.SNAPSHOT) {
          paused = true;
          signal.value = event.value;
          paused = false;
        }
      });

    signal.subscribe((value) => {
      if (!paused) {
        onUpdate(
          this.#client.call(ENDPOINT, 'update', {
            clientSignalId: this.#id,
            event: { id: nanoid(), type: StateEventType.SET, value },
          }),
        );
      }
    });
  }
}
