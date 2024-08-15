import { signal } from '@preact/signals-react';
import type { ConnectClient, Subscription } from '@vaadin/hilla-frontend';
import { nanoid } from 'nanoid';
import { Signal } from './core.js';

const ENDPOINT = 'SignalsHandler';

type SubscriptionEvent<T> = Readonly<{
  type: StateEventType;
  value: T;
}>;

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
 * Options for creating a full-stack signal.
 */
export type FullStackSignalOptions = Readonly<{
  /**
   * The client instance to be used for communication.
   */
  client: ConnectClient;
  /**
   * The method name of the signal provider service.
   */
  method: string;
}>;

class SubscriptionManager<T> {
  readonly #client;
  readonly #method: string;
  readonly #id: string;
  #subscription?: Subscription<StateEvent<T>>;

  constructor(client: ConnectClient, method: string, id: string) {
    this.#client = client;
    this.#method = method;
    this.#id = id;
  }

  get subscription() {
    return this.#subscription;
  }

  subscribe() {
    this.#subscription ??= this.#client.subscribe(ENDPOINT, 'subscribe', {
      signalProviderEndpointMethod: this.#method,
      clientSignalId: this.#id,
    });

    return this.#subscription;
  }

  async call(event: SubscriptionEvent<T>): Promise<void> {
    await this.#client.call(ENDPOINT, 'update', {
      clientSignalId: this.#id,
      event,
    });
  }

  cancel() {
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
  readonly id = nanoid();
  readonly #pending = signal(false);
  readonly #error = signal<Error | undefined>(undefined);
  readonly #manager: SubscriptionManager<T>;

  constructor(value: T | undefined, { client, method }: FullStackSignalOptions) {
    super(value);
    this.#manager = new SubscriptionManager(client, method, this.id);

    let paused = false;
    this.#manager.subscribe().onNext((event: StateEvent<T>) => {
      if (event.type === StateEventType.SNAPSHOT) {
        paused = true;
        this.value = event.value;
        paused = false;
      }
    });

    this.subscribe((v) => {
      if (!paused) {
        this.#pending.value = true;
        this.#manager
          .call({
            type: StateEventType.SET,
            value: v,
          })
          .catch((error) => {
            this.#error.value = error;
          })
          .finally(() => {
            this.#pending.value = false;
          });
      }
    });
  }

  /**
   * Defines whether the signal is currently awaits a server-side response.
   */
  get pending(): boolean {
    return this.#pending.value;
  }

  /**
   * Defines whether the signal has an error.
   */
  get error(): Error | undefined {
    return this.#error.value;
  }

  get updating(): Promise<void> {
    return new Promise((resolve) => {
      const unsubscribe = this.#pending.subscribe((value) => {
        if (value) {
          resolve();
          unsubscribe();
        }
      });
    });
  }

  /**
   * Cancels the subscription to the server-side signal provider.
   */
  cancel(): void {
    this.#manager.cancel();
  }
}
