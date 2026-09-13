import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Entity from './com/vaadin/hilla/parser/plugins/nonnull/extended/ExtendedEndpoint/Entity.js';
import client from './connect-client.default.js';

export async function getNonnullListOfNullableElements(init?: EndpointRequestInit): Promise<Array<Entity | undefined>> {
  return client.call('ExtendedEndpoint', 'getNonnullListOfNullableElements', {}, init);
}

export async function superComplexType(
  list: Array<Record<string, Array<Record<string, string> | undefined> | undefined> | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Array<Record<string, Array<Record<string, string> | undefined> | undefined> | undefined> | undefined> {
  return client.call('ExtendedEndpoint', 'superComplexType', { list }, init);
}
