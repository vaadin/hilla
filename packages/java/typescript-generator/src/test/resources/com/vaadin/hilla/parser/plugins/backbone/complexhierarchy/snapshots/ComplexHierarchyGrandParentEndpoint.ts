import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function executeGrandParentEndpointMethod(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('ComplexHierarchyGrandParentEndpoint', 'executeGrandParentEndpointMethod', {}, init);
}
