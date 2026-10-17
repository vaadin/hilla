import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type EnumEntity from './com/vaadin/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/EnumEntity.js';
import type RenamedEnum from './com/vaadin/hilla/parser/plugins/backbone/enumtype/EnumTypeEndpoint/RenamedEnum.js';
import client from './connect-client.default.js';

async function echoEnum(value: EnumEntity | undefined, init?: EndpointRequestInit): Promise<EnumEntity | undefined> {
  return client.call('EnumTypeEndpoint', 'echoEnum', { value }, init);
}

async function echoListEnum(enumList: Array<EnumEntity | undefined> | undefined, init?: EndpointRequestInit): Promise<Array<EnumEntity | undefined> | undefined> {
  return client.call('EnumTypeEndpoint', 'echoListEnum', { enumList }, init);
}

async function echoRenamed(value: RenamedEnum | undefined, init?: EndpointRequestInit): Promise<RenamedEnum | undefined> {
  return client.call('EnumTypeEndpoint', 'echoRenamed', { value }, init);
}

async function getEnum(init?: EndpointRequestInit): Promise<EnumEntity | undefined> {
  return client.call('EnumTypeEndpoint', 'getEnum', {}, init);
}

async function setEnum(value: EnumEntity | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('EnumTypeEndpoint', 'setEnum', { value }, init);
}

export { echoEnum, echoListEnum, echoRenamed, getEnum, setEnum };
