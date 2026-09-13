import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Order from './com/vaadin/hilla/mappedtypes/Order.js';
import type Pageable from './com/vaadin/hilla/mappedtypes/Pageable.js';
import type Sort from './com/vaadin/hilla/mappedtypes/Sort.js';
import client from './connect-client.default.js';

export async function getOrder(init?: EndpointRequestInit): Promise<Order | undefined> {
  return client.call('PageableEndpoint', 'getOrder', {}, init);
}

export async function getPage(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
  return client.call('PageableEndpoint', 'getPage', {}, init);
}

export async function getPageable(init?: EndpointRequestInit): Promise<Pageable | undefined> {
  return client.call('PageableEndpoint', 'getPageable', {}, init);
}

export async function getSort(init?: EndpointRequestInit): Promise<Sort | undefined> {
  return client.call('PageableEndpoint', 'getSort', {}, init);
}
