import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
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
 * A simple timeout helper class.
 */
class Timeout {
  readonly #timeout: number;
  readonly #callback: () => void;
  #timeoutId?: ReturnType<typeof setTimeout>;

  constructor(callback: () => void, timeout: number) {
    this.#callback = callback;
    this.#timeout = timeout;
  }

  restart() {
    if (this.#timeoutId) {
      clearTimeout(this.#timeoutId);
    }

    this.#timeoutId = setTimeout(this.#callback, this.#timeout);
  }
}

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
  readonly #timeout: Timeout;
  readonly #client: ConnectClient;
  readonly #method: string;
  #subscription?: Subscription<StateEvent>;

  /**
   * @param client - The client instance to be used for communication.
   * @param signalProviderEndpointMethod - The method name of the signal provider
   * service.
   * @param timeout - The timeout in milliseconds after which the connection
   * will be closed. The timeout is restarted every time a new signal is
   * connected or a signal value is updated. Default is 2000 ms.
   *
   * @returns The signal channel instance.
   */
  constructor(client: ConnectClient, signalProviderEndpointMethod: string, timeout = 2000) {
    this.#client = client;
    this.#method = signalProviderEndpointMethod;
    this.#timeout = new Timeout(() => this.#subscription?.cancel(), timeout);
  }

  get id(): string {
    return this.#id;
  }

  /**
   * Connects the local signal to the server.
   *
   * @param signal - The signal instance to be connected.
   */
  connect(signal: ValueSignal): void {
    this.#timeout.restart();

    this.#subscription ??= this.#client
      .subscribe(ENDPOINT, 'subscribe', {
        signalProviderEndpointMethod: this.#method,
        clientSignalId: this.#id,
      })
      .onNext((event: StateEvent) => {
        if (event.type === StateEventType.SNAPSHOT) {
          signal.value = event.value;
        }
      });

    signal.subscribe((value, done) => {
      this.#timeout.restart();

      done(
        this.#client.call(ENDPOINT, 'update', {
          clientSignalId: this.#id,
          event: { id: nanoid(), type: StateEventType.SET, value },
        }),
      );
    });
  }
}
