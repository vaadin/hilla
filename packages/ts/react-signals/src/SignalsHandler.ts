import { ConnectClient, type EndpointRequestInit, type Subscription } from '@vaadin/hilla-frontend';

export default class SignalsHandler {
  private readonly client: ConnectClient;

  constructor(client: ConnectClient) {
    this.client = client;
  }

  subscribe(signalProviderEndpointMethod: string, clientSignalId: string): Subscription<string> {
    return this.client.subscribe('SignalsHandler', 'subscribe', { signalProviderEndpointMethod, clientSignalId });
  }

  async update(clientSignalId: string, event: string, init?: EndpointRequestInit): Promise<void> {
    return this.client.call('SignalsHandler', 'update', { clientSignalId, event }, init);
  }
}
