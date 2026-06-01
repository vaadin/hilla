import type { ConnectClient, EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';
import type { SignalCommand } from './commands.js';

const ENDPOINT = 'SignalsHandler';

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
export class Connection {
  readonly #id: string;
  readonly config: ServerConnectionConfig;
  #subscription?: Subscription<SignalCommand>;

  constructor(id: string, config: ServerConnectionConfig) {
    this.config = config;
    this.#id = id;
  }

  get subscription(): Subscription<SignalCommand> | undefined {
    return this.#subscription;
  }

  connect(): Subscription<SignalCommand> {
    const { client, endpoint, method, params } = this.config;

    this.#subscription ??= client.subscribe(ENDPOINT, 'subscribe', {
      providerEndpoint: endpoint,
      providerMethod: method,
      clientSignalId: this.#id,
      params,
    });

    return this.#subscription;
  }

  async send(command: SignalCommand, init?: EndpointRequestInit): Promise<void> {
    const onTheFly = !this.#subscription;

    if (onTheFly) {
      this.connect();
    }

    await this.config.client.call(
      ENDPOINT,
      'update',
      {
        clientSignalId: this.#id,
        command,
      },
      init ?? { mute: true },
    );

    if (onTheFly) {
      this.disconnect();
    }
  }

  disconnect(): void {
    this.#subscription?.cancel();
    this.#subscription = undefined;
  }
}
