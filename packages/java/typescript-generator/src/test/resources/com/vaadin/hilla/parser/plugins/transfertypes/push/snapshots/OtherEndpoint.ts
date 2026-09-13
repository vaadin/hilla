import type { EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export function getMessageFlux(count: number): Subscription<string | undefined> {
  return client.subscribe('OtherEndpoint', 'getMessageFlux', { count });
}

export async function toUpperCase(
  message: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('OtherEndpoint', 'toUpperCase', { message }, init);
}
