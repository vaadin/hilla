import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Nested from './com/vaadin/hilla/parser/plugins/model/nested/NestedEndpoint/Nested.js';
import client from './connect-client.default.js';

async function getNested(init?: EndpointRequestInit): Promise<Nested | undefined> {
  return client.call('NestedEndpoint', 'getNested', {}, init);
}

async function transpose(matrix: Array<Array<number> | undefined> | undefined, init?: EndpointRequestInit): Promise<Array<Array<number> | undefined> | undefined> {
  return client.call('NestedEndpoint', 'transpose', { matrix }, init);
}

export { getNested, transpose };
