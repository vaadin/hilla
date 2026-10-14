import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function price(count: number | undefined, init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('ValueSubclassEndpoint', 'price', { count }, init);
}
