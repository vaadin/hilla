import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Person from './com/vaadin/hilla/parser/plugins/nonnull/kotlin/superclasses/javaendpointexposed/PersonEndpoint/Person.js';
import client from './connect-client.default.js';

export async function get(id: number | undefined, init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'get', { id }, init);
}

async function _delete(id: number | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('PersonEndpoint', 'delete', { id }, init);
}

export async function update(entity: Person | undefined, init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'update', { entity }, init);
}

export async function getNonNullablePage(
  pageSize: number,
  pageNumber: number,
  parameters: Record<string, Person | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Array<Person | undefined> | undefined> {
  return client.call('PersonEndpoint', 'getNonNullablePage', { pageSize, pageNumber, parameters }, init);
}

export async function getPage(
  pageSize: number,
  pageNumber: number,
  init?: EndpointRequestInit,
): Promise<Array<Person | undefined> | undefined> {
  return client.call('PersonEndpoint', 'getPage', { pageSize, pageNumber }, init);
}

export async function size(init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'size', {}, init);
}

export async function create(entity: Person, init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'create', { entity }, init);
}

export { _delete as delete };
