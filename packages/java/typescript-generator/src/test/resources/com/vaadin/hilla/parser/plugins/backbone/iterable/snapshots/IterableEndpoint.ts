import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Foo from './com/vaadin/hilla/parser/plugins/backbone/iterable/IterableEndpoint/Foo.js';
import client from './connect-client.default.js';

export async function getFooAnotherCustomIterable(
  init?: EndpointRequestInit,
): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooAnotherCustomIterable', {}, init);
}

export async function getFooArray(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooArray', {}, init);
}

export async function getFooCustomIterable(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooCustomIterable', {}, init);
}

export async function getFooIterable(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooIterable', {}, init);
}

export async function getFooList(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooList', {}, init);
}

export async function getFooSet(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFooSet', {}, init);
}

export async function getSpecializedIterable(
  init?: EndpointRequestInit,
): Promise<Array<string | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getSpecializedIterable', {}, init);
}

export async function getSpecializedIterableCustom(
  init?: EndpointRequestInit,
): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getSpecializedIterableCustom', {}, init);
}
