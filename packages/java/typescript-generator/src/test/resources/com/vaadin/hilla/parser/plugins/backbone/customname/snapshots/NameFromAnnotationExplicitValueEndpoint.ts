import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function doSomething(init?: EndpointRequestInit): Promise<void> {
  return client.call('NameFromAnnotationExplicitValueEndpoint', 'doSomething', {}, init);
}
