import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Open from './com/vaadin/hilla/parser/plugins/backbone/opensubtype/OpenSubtypeEndpoint/Open.js';
import client from './connect-client.default.js';

async function getOpen(init?: EndpointRequestInit): Promise<Open | undefined> {
  return client.call('OpenSubtypeEndpoint', 'getOpen', {}, init);
}

export { getOpen };
