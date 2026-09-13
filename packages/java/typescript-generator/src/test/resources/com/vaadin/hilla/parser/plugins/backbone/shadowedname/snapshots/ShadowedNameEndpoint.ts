import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Collection from './com/vaadin/hilla/parser/plugins/backbone/shadowedname/ShadowedNameEndpoint/Collection.js';
import type Collection_1 from './com/vaadin/hilla/parser/plugins/backbone/shadowedname/subpackage/Collection.js';
import client from './connect-client.default.js';

export async function getJavaCollection(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
  return client.call('ShadowedNameEndpoint', 'getJavaCollection', {}, init);
}

export async function getNestedUserDefinedCollection(
  name: string | undefined,
  init?: EndpointRequestInit,
): Promise<Collection | undefined> {
  return client.call('ShadowedNameEndpoint', 'getNestedUserDefinedCollection', { name }, init);
}

export async function getSeparateUserDefinedCollection(
  init?: EndpointRequestInit,
): Promise<Collection_1<string | undefined> | undefined> {
  return client.call('ShadowedNameEndpoint', 'getSeparateUserDefinedCollection', {}, init);
}
