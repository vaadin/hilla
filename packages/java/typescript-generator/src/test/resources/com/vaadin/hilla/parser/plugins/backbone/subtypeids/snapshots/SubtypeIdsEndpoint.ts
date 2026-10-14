import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Pet from './com/vaadin/hilla/parser/plugins/backbone/subtypeids/SubtypeIdsEndpoint/Pet.js';
import type Wrapped from './com/vaadin/hilla/parser/plugins/backbone/subtypeids/SubtypeIdsEndpoint/Wrapped.js';
import client from './connect-client.default.js';

export async function getPet(init?: EndpointRequestInit): Promise<Pet | undefined> {
  return client.call('SubtypeIdsEndpoint', 'getPet', {}, init);
}

export async function getWrapped(init?: EndpointRequestInit): Promise<Wrapped | undefined> {
  return client.call('SubtypeIdsEndpoint', 'getWrapped', {}, init);
}
