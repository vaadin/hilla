import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type String from './com/vaadin/hilla/parser/plugins/backbone/nameclash/StringEndpoint/String.js';
import client from './connect-client.default.js';

async function get(init?: EndpointRequestInit): Promise<String | undefined> {
  return client.call('StringEndpoint', 'get', {}, init);
}

export { get };
