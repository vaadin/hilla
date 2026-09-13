import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type CustomConfigEntity from './com/vaadin/hilla/parser/plugins/backbone/config/CustomConfigEndpoint/CustomConfigEntity.js';
import client from './connect-client.default.js';

export async function get(init?: EndpointRequestInit): Promise<CustomConfigEntity | undefined> {
  return client.call('CustomConfigEndpoint', 'get', {}, init);
}
