import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Ordered from './com/vaadin/hilla/parser/plugins/backbone/accessororder/AccessorOrderEndpoint/Ordered.js';
import client from './connect-client.default.js';

async function get(init?: EndpointRequestInit): Promise<Ordered | undefined> {
  return client.call('AccessorOrderEndpoint', 'get', {}, init);
}

export { get };
