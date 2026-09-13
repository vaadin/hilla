import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function genericMethod(
  param: string | undefined,
  init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('GenericSuperClassString', 'genericMethod', { param }, init);
}
