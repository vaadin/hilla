import type {
  ActionOnLostSubscription,
  ConnectClient,
  EndpointRequestInit,
  Subscription,
} from '@vaadin/hilla-frontend';
import { createSetCommand, type SignalCommand } from './commands.js';
import { computed, signal, Signal } from './core.js';
import { randomId } from './utils.js';

const ENDPOINT = 'SignalsHandler';

/**
 * A return type for signal operations that exposes a `result` property of type
 * `Promise`, that resolves when the operation is completed. It allows defining
 * callbacks to be run after the operation is completed, or error handling when
 * the operation fails.
 *
 * @example
 * ```ts
 * const sharedName = NameService.sharedName({ defaultValue: '' });
 * sharedName.replace('John').result
 *    .then(() => console.log('Name updated successfully'))
 *    .catch((error) => console.error('Failed to update the name:', error));
 * ```
 */
export interface Operation {
  result: Promise<void>;
}

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
class ServerConnection {
  readonly #id: string;
  readonly config: ServerConnectionConfig;
  #subscription?: Subscription<SignalCommand>;

  constructor(id: string, config: ServerConnectionConfig) {
    this.config = config;
    this.#id = id;
  }

  get subscription() {
    return this.#subscription;
  }

  connect() {
    const { client, endpoint, method, params } = this.config;

    this.#subscription ??= client.subscribe(ENDPOINT, 'subscribe', {
      providerEndpoint: endpoint,
      providerMethod: method,
      clientSignalId: this.#id,
      params,
    });

    return this.#subscription;
  }

  async update(command: SignalCommand, init?: EndpointRequestInit): Promise<void> {
    const onTheFly = !this.#subscription;

    if (onTheFly) {
      this.connect();
    }

    await this.config.client.call(
      ENDPOINT,
      'update',
      {
        clientSignalId: this.#id,
        event: command,
      },
      init ?? { mute: true },
    );

    if (onTheFly) {
      this.disconnect();
    }
  }

  disconnect() {
    this.#subscription?.cancel();
    this.#subscription = undefined;
  }
}

export const $update = Symbol('update');
export const $processServerResponse = Symbol('processServerResponse');
export const $setValueQuietly = Symbol('setValueQuietly');
export const $resolveOperation = Symbol('resolveOperation');
export const $createOperation = Symbol('createOperation');

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
  readonly id: string;

  /**
   * The server connection manager.
   */
  readonly server: ServerConnection;

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

  /**
   * Optional parent signal for command routing.
   */
  protected readonly parent?: FullStackSignal<any>;

  constructor(value: T | undefined, config: ServerConnectionConfig, id?: string, parent?: FullStackSignal<any>) {
    super(
      value,
      () => (!parent ? this.#connect() : undefined),
      () => (!parent ? this.#disconnect() : undefined),
    );
    this.id = id ?? randomId();
    this.server = new ServerConnection(this.id, config);
    this.parent = parent;

    this.subscribe((v) => {
      if (!this.#paused) {
        this.#pending.value = true;
        this.#error.value = undefined;
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this[$update](createSetCommand('', v));
      }
    });

    this.#paused = false;
  }

  // stores the promise handlers associated to operations
  readonly #operationPromises = new Map<
    string,
    {
      resolve(value: PromiseLike<void> | void): void;
      reject(reason?: any): void;
    }
  >();

  // creates the object to be returned by operations to allow defining callbacks
  protected [$createOperation]({ id, promise }: { id?: string; promise?: Promise<void> }): Operation {
    const thens = this.#operationPromises;
    const promises: Array<Promise<void>> = [];

    if (promise) {
      // Add the provided promise to the list of promises
      promises.push(promise);
    }

    if (id) {
      // Create a promise to be associated to the provided id
      promises.push(
        new Promise<void>((resolve, reject) => {
          thens.set(id, { resolve, reject });
        }),
      );
    }

    if (promises.length === 0) {
      // If no promises were added, return a resolved promise
      promises.push(Promise.resolve());
    }

    return {
      result: Promise.allSettled(promises).then((results) => {
        const lastResult = results[results.length - 1];
        if (lastResult.status === 'fulfilled') {
          return undefined;
        }
        throw lastResult.reason;
      }),
    };
  }

  /**
   * Sets the local value of the signal without sending any events to the server
   * @param value - The new value.
   * @internal
   */
  protected [$setValueQuietly](value: T): void {
    this.#paused = true;
    super.value = value;
    this.#paused = false;
  }

  /**
   * A method to update the server with the new value or via parent.
   *
   * @param command - The command to update the server with.
   * @returns The server response promise.
   */
  protected async [$update](command: SignalCommand): Promise<void> {
    if (this.parent) {
      // Route command via parent, set targetNodeId
      const routedCommand = { ...command, targetNodeId: this.id };
      return this.parent[$update](routedCommand);
    }
    return this.server
      .update(command)
      .catch((error: unknown) => {
        this.#error.value = error instanceof Error ? error : new Error(String(error));
      })
      .finally(() => {
        this.#pending.value = false;
      });
  }

  /**
   * Resolves the operation promise associated with the given event id.
   *
   * @param commandId - The command id.
   * @param reason - The reason to reject the promise (if any).
   */
  protected [$resolveOperation](commandId: string, reason?: string): void {
    const operationPromise = this.#operationPromises.get(commandId);
    if (operationPromise) {
      this.#operationPromises.delete(commandId);
      if (reason) {
        operationPromise.reject(reason);
      } else {
        operationPromise.resolve();
      }
    }
  }

  /**
   * A method with to process the server response. The implementation is
   * specific for each signal type.
   *
   * @param command - The server response command.
   */
  protected abstract [$processServerResponse](command: SignalCommand): void;

  #connect() {
    this.server
      .connect()
      .onSubscriptionLost(() => 'resubscribe' as ActionOnLostSubscription)
      .onNext((command: SignalCommand) => {
        this.#paused = true;
        this[$processServerResponse](command);
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
