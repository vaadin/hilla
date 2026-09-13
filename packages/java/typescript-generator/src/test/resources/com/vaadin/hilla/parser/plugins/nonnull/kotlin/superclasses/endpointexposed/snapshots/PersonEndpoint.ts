import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Person from './com/vaadin/hilla/parser/plugins/nonnull/kotlin/superclasses/endpointexposed/PersonEndpoint/Person.js';
import client from './connect-client.default.js';

export async function get(id: number, init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'get', { id }, init);
}

async function _delete(id: number, init?: EndpointRequestInit): Promise<void> {
  return client.call('PersonEndpoint', 'delete', { id }, init);
}

export async function save(entity: Person | undefined, init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'save', { entity }, init);
}

export async function update(entity: Person, init?: EndpointRequestInit): Promise<Person> {
  return client.call('PersonEndpoint', 'update', { entity }, init);
}

export async function getNonNullablePage(
  pageSize: number,
  pageNumber: number,
  parameters: Record<string, Person>,
  init?: EndpointRequestInit,
): Promise<Array<Person>> {
  return client.call('PersonEndpoint', 'getNonNullablePage', { pageSize, pageNumber, parameters }, init);
}

export async function getPage(
  pageSize: number,
  pageNumber: number,
  init?: EndpointRequestInit,
): Promise<Array<Person>> {
  return client.call('PersonEndpoint', 'getPage', { pageSize, pageNumber }, init);
}

export async function size(init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'size', {}, init);
}

export async function create(entity: Person, init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'create', { entity }, init);
}

export { _delete as delete };
