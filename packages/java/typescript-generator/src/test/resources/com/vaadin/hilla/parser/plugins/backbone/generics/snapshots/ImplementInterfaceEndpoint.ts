import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type ConcreteType from './com/vaadin/hilla/parser/plugins/backbone/generics/ConcreteType.js';
import client from './connect-client.default.js';

export async function dealWithConcreteType(
  object: ConcreteType | undefined,
  init?: EndpointRequestInit,
): Promise<ConcreteType | undefined> {
  return client.call('ImplementInterfaceEndpoint', 'dealWithConcreteType', { object }, init);
}

export async function dealWithGenericType(
  object: ConcreteType | undefined,
  init?: EndpointRequestInit,
): Promise<ConcreteType | undefined> {
  return client.call('ImplementInterfaceEndpoint', 'dealWithGenericType', { object }, init);
}

export async function dealWithItAgain(
  object: ConcreteType | undefined,
  init?: EndpointRequestInit,
): Promise<ConcreteType | undefined> {
  return client.call('ImplementInterfaceEndpoint', 'dealWithItAgain', { object }, init);
}
