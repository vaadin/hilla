import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

async function shared(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('InheritingEndpoint', 'shared', {}, init);
}

async function twice(one: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('InheritingEndpoint', 'twice', { one }, init);
}

export { shared, twice };
