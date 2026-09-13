import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type EnumEntity from './com/vaadin/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/EnumEntity.js';
import client from './connect-client.default.js';

export async function echoEnum(
  value: EnumEntity | undefined,
  init?: EndpointRequestInit,
): Promise<EnumEntity | undefined> {
  return client.call('EnumTypeEndpoint', 'echoEnum', { value }, init);
}

export async function echoListEnum(
  enumList: Array<EnumEntity | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Array<EnumEntity | undefined> | undefined> {
  return client.call('EnumTypeEndpoint', 'echoListEnum', { enumList }, init);
}

export async function getEnum(init?: EndpointRequestInit): Promise<EnumEntity | undefined> {
  return client.call('EnumTypeEndpoint', 'getEnum', {}, init);
}

export async function setEnum(value: EnumEntity | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('EnumTypeEndpoint', 'setEnum', { value }, init);
}
