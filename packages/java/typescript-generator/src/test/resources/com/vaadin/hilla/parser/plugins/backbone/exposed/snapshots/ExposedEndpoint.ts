import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type ExposedInterfaceEntity from './com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedInterfaceEntity.js';
import type ExposedSuperclassEntity from './com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedSuperclassEntity.js';
import client from './connect-client.default.js';

export async function methodFromExposedSuperclass(
  init?: EndpointRequestInit,
): Promise<ExposedSuperclassEntity | undefined> {
  return client.call('ExposedEndpoint', 'methodFromExposedSuperclass', {}, init);
}

export async function methodFromExposedInterface(
  init?: EndpointRequestInit,
): Promise<ExposedInterfaceEntity | undefined> {
  return client.call('ExposedEndpoint', 'methodFromExposedInterface', {}, init);
}

export async function methodFromEndpoint(init?: EndpointRequestInit): Promise<void> {
  return client.call('ExposedEndpoint', 'methodFromEndpoint', {}, init);
}
