import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function arrayNode(
  node: Array<unknown> | undefined,
  init?: EndpointRequestInit,
): Promise<Array<unknown> | undefined> {
  return client.call('JsonNodeEndpoint', 'arrayNode', { node }, init);
}

export async function jsonNode(node: unknown, init?: EndpointRequestInit): Promise<unknown> {
  return client.call('JsonNodeEndpoint', 'jsonNode', { node }, init);
}

export async function objectNode(node: unknown, init?: EndpointRequestInit): Promise<unknown> {
  return client.call('JsonNodeEndpoint', 'objectNode', { node }, init);
}
