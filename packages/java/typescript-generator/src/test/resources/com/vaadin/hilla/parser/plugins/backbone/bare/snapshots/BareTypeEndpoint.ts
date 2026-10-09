import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function getBareList(init?: EndpointRequestInit): Promise<Array<unknown> | undefined> {
  return client.call('BareTypeEndpoint', 'getBareList', {}, init);
}

export async function getBareMap(init?: EndpointRequestInit): Promise<Record<string, unknown> | undefined> {
  return client.call('BareTypeEndpoint', 'getBareMap', {}, init);
}

export async function getBareOptional(init?: EndpointRequestInit): Promise<unknown | undefined> {
  return client.call('BareTypeEndpoint', 'getBareOptional', {}, init);
}
