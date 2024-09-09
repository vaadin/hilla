import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import { computed, signal, Signal } from './core.js';
import { createSetStateEvent, type StateEvent } from './events.js';

const ENDPOINT = 'SignalsHandler';

/**
 * An abstraction of a signal that tracks the number of subscribers, and calls
 * the provided `onSubscribe` and `onUnsubscribe` callbacks for the first
 * subscription and the last unsubscription, respectively.
 * @internal
 */
export abstract class DependencyTrackingSignal<T> extends Signal<T> {
  readonly #onFirstSubscribe: () => void;
  readonly #onLastUnsubscribe: () => void;

  // -1 means to ignore the first subscription that is created internally in the
  // FullStackSignal constructor.
  #subscribeCount = -1;

  protected constructor(value: T | undefined, onFirstSubscribe: () => void, onLastUnsubscribe: () => void) {
    super(value);
    this.#onFirstSubscribe = onFirstSubscribe;
    this.#onLastUnsubscribe = onLastUnsubscribe;
  }

  protected override S(node: unknown): void {
    super.S(node);
    if (this.#subscribeCount === 0) {
      this.#onFirstSubscribe();
    }
    this.#subscribeCount += 1;
  }

  protected override U(node: unknown): void {
    super.U(node);
    this.#subscribeCount -= 1;
    if (this.#subscribeCount === 0) {
      this.#onLastUnsubscribe();
    }
  }
}

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

  /**
   * Optional object with method call arguments to be sent to the endpoint
   * method that provides the signal when subscribing to it.
   */
  params?: Record<string, unknown>;
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
    const { client, endpoint, method, params } = this.#config;

    this.#subscription ??= client.subscribe(ENDPOINT, 'subscribe', {
      providerEndpoint: endpoint,
      providerMethod: method,
      clientSignalId: this.#id,
      params,
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

export const $update = Symbol('update');
export const $processServerResponse = Symbol('processServerResponse');

/**
 * A signal that holds a shared value. Each change to the value is propagated to
 * the server-side signal provider. At the same time, each change received from
 * the server-side signal provider is propagated to the local signal and it's
 * subscribers.
 *
 * @internal
 */
export abstract class FullStackSignal<T> extends DependencyTrackingSignal<T> {
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

  // Paused at the very start to prevent the signal from sending the initial
  // value to the server.
  #paused = true;

  constructor(value: T | undefined, config: ServerConnectionConfig) {
    super(
      value,
      () => this.#connect(),
      () => this.#disconnect(),
    );
    this.server = new ServerConnection(this.id, config);

    this.subscribe((v) => {
      if (!this.#paused) {
        this.#pending.value = true;
        this.#error.value = undefined;
        this[$update](createSetStateEvent(v));
      }
    });

    this.#paused = false;
  }

  /**
   * A method to update the server with the new value.
   *
   * @param event - The event to update the server with.
   */
  protected [$update](event: StateEvent<T>): void {
    this.server
      .update(event)
      .catch((error: unknown) => {
        this.#error.value = error instanceof Error ? error : new Error(String(error));
      })
      .finally(() => {
        this.#pending.value = false;
      });
  }

  /**
   * A method with to process the server response. The implementation is
   * specific for each signal type.
   *
   * @param event - The server response event.
   */
  protected abstract [$processServerResponse](event: StateEvent<T>): void;

  #connect() {
    this.server.connect().onNext((event: StateEvent<T>) => {
      this.#paused = true;
      this[$processServerResponse](event);
      this.#paused = false;
    });
  }

  #disconnect() {
    if (this.server.subscription === undefined) {
      return;
    }
    this.server.disconnect();
  }
}
