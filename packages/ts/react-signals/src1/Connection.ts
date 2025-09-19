import type { ConnectClient, EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';
import type { SignalCommand } from './commands.js';

/**
 * Configuration object for establishing a server connection to a signal
 * provider service. This type defines the necessary parameters to connect and
 * subscribe to a server-side signal.
 */
export type ConnectionConfig = Readonly<{
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
  params?: Readonly<Record<string, unknown>>;
}>;

const ENDPOINT = 'SignalsHandler';

/**
 * Manages a client-server connection for {@link FullstackSignal}, handling
 * subscription, updates, and disconnections.
 *
 * This class facilitates communication with a server endpoint for signal-based
 * data synchronization. It supports lazy connection establishment and temporary
 * connections for one-off updates.
 *
 * @param id - A unique identifier for the signal instance.
 * @param config - The configuration object for the server connection.
 *
 * @example
 * ```ts
 * const config: ServerConnectionConfig = { client, endpoint: 'myEndpoint', method: 'myMethod', params: {} };
 * const connection = new ServerConnection('signalId', config);
 * const subscription = connection.connect();
 * await connection.update({ type: 'update', data: 'newValue' });
 * connection.disconnect();
 * ```
 */
export class Connection {
  readonly #id: number;
  readonly config: ConnectionConfig;
  #subscription?: Subscription<SignalCommand>;

  constructor(id: number, config: ConnectionConfig) {
    this.config = config;
    this.#id = id;
  }

  get subscription(): Subscription<SignalCommand> | undefined {
    return this.#subscription;
  }

  /**
   * Establishes a permanent connection to the server for signal updates.
   * If a connection already exists, it returns the existing one.
   *
   * @returns A subscription object for handling signal commands.
   */
  establish(): Subscription<SignalCommand> {
    const { client, endpoint, method, params } = this.config;

    this.#subscription ??= client.subscribe(ENDPOINT, 'subscribe', {
      providerEndpoint: endpoint,
      providerMethod: method,
      clientSignalId: this.#id,
      params,
    });

    return this.#subscription;
  }

  /**
   * Updates the signal on the server by sending the provided command.
   * If no connection exists, establishes a temporary connection for the
   * update that is terminated afterward.
   *
   * @param command - The signal command to send to the server.
   * @param init - Optional request initialization options. Defaults to
   * `{ mute: true }` if not provided.
   * @returns A promise that resolves when the update is complete.
   */
  async send(command: SignalCommand, init?: EndpointRequestInit): Promise<SignalCommand> {
    const temporary = !this.#subscription;

    if (temporary) {
      this.establish();
    }

    let promise = this.config.client.call(
      ENDPOINT,
      'update',
      {
        targetId: this.#id,
        command,
      },
      init ?? { mute: true },
    );

    if (temporary) {
      promise = promise.finally(() => this.terminate());
    }

    const result = new Promise<SignalCommand>((resolve) => {
      this.#subscription?.onNext((received) => {
        if (received.id === command.id) {
          resolve(received);
        }
      });
    });

    const [, resultCommand] = await Promise.all([promise, result]);

    return resultCommand;
  }

  /**
   * Terminates the current connection.
   */
  terminate(): void {
    this.#subscription?.cancel();
    this.#subscription = undefined;
  }
}
