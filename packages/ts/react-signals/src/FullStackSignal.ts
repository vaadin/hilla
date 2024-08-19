import { computed, signal } from '@preact/signals-react';
import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import { Signal } from './core.js';

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
export type StateEvent<T> = Readonly<{
  id: string;
  type: StateEventType;
  value: T;
}>;

/**
 * An object that describes a data object to connect to the signal provider
 * service.
 */
export type ServerConnectionConfig = Readonly<{
  /**
   * The client instance to be used for communication.
   */
  client: ConnectClient;

  /**
   * The name of the signal provider service endpoint.
   */
  endpoint: string;

  /**
   * The name of the signal provider service method.
   */
  method: string;
}>;

/**
 * A server connection manager.
 */
class ServerConnection<T> {
  readonly #id: string;
  readonly #config: ServerConnectionConfig;
  #subscription?: Subscription<StateEvent<T>>;

  constructor(id: string, config: ServerConnectionConfig) {
    this.#config = config;
    this.#id = id;
  }

  get subscription() {
    return this.#subscription;
  }

  connect() {
    const { client, endpoint, method } = this.#config;

    this.#subscription ??= client.subscribe(ENDPOINT, 'subscribe', {
      providerEndpoint: endpoint,
      providerMethod: method,
      clientSignalId: this.#id,
    });

    return this.#subscription;
  }

  async update(event: StateEvent<T>): Promise<void> {
    await this.#config.client.call(ENDPOINT, 'update', {
      clientSignalId: this.#id,
      event,
    });
  }

  disconnect() {
    this.#subscription?.cancel();
    this.#subscription = undefined;
  }
}

/**
 * A signal that holds a shared value. Each change to the value is propagated to
 * the server-side signal provider. At the same time, each change received from
 * the server-side signal provider is propagated to the local signal and it's
 * subscribers.
 *
 * @internal
 */
export abstract class FullStackSignal<T> extends Signal<T> {
  /**
   * The unique identifier of the signal necessary to communicate with the
   * server.
   */
  readonly id = nanoid();

  /**
   * The server connection manager.
   */
  readonly server: ServerConnection<T>;

  /**
   * Defines whether the signal is currently awaits a server-side response.
   */
  readonly pending = computed(() => this.#pending.value);

  /**
   * Defines whether the signal has an error.
   */
  readonly error = computed(() => this.#error.value);

  readonly #pending = signal(false);
  readonly #error = signal<Error | undefined>(undefined);

  constructor(value: T | undefined, config: ServerConnectionConfig) {
    super(value);
    this.server = new ServerConnection(this.id, config);

    // Paused at the very start to prevent the signal from sending the initial
    // value to the server.
    let paused = true;

    this.server.connect().onNext((event: StateEvent<T>) => {
      if (event.type === StateEventType.SNAPSHOT) {
        paused = true;
        this.value = event.value;
        paused = false;
      }
    });

    this.subscribe((v) => {
      if (!paused) {
        this.#pending.value = true;
        this.#error.value = undefined;
        this.server
          .update({
            id: nanoid(),
            type: StateEventType.SET,
            value: v,
          })
          .catch((error: unknown) => {
            this.#error.value = error instanceof Error ? error : new Error(String(error));
          })
          .finally(() => {
            this.#pending.value = false;
          });
      }
    });

    paused = false;
  }
}
