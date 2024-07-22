import type { ConnectClient, EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';

/**
 * SignalsHandler is a helper class for handling the
 * communication of the full-stack signal instances
 * and their server-side counterparts they are
 * subscribed and publish their updates to.
 */
export default class SignalsHandler {
  readonly #client: ConnectClient;

  constructor(client: ConnectClient) {
    this.#client = client;
  }

  subscribe(signalProviderEndpointMethod: string, clientSignalId: string): Subscription<string> {
    return this.#client.subscribe('SignalsHandler', 'subscribe', { signalProviderEndpointMethod, clientSignalId });
  }

  async update(clientSignalId: string, event: string, init?: EndpointRequestInit): Promise<void> {
    return this.#client.call('SignalsHandler', 'update', { clientSignalId, event }, init);
  }
}
