import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type MultiEndpointsSharedModel from './com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js';
import client from './connect-client.default.js';

export async function getFoo(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('MultiEndpointsFooEndpoint', 'getFoo', {}, init);
}

export async function getShared(init?: EndpointRequestInit): Promise<MultiEndpointsSharedModel | undefined> {
  return client.call('MultiEndpointsFooEndpoint', 'getShared', {}, init);
}
