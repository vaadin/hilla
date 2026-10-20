import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client_1 from './connect-client.default.js';

async function client(init?: EndpointRequestInit): Promise<string | undefined> {
  return client_1.call('ClientMethodEndpoint', 'client', {}, init);
}

export { client };
