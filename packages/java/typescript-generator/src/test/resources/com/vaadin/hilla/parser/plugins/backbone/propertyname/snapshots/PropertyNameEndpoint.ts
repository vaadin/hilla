import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Named from './com/vaadin/hilla/parser/plugins/backbone/propertyname/PropertyNameEndpoint/Named.js';
import client from './connect-client.default.js';

async function get(init?: EndpointRequestInit): Promise<Named | undefined> {
  return client.call('PropertyNameEndpoint', 'get', {}, init);
}

export { get };
