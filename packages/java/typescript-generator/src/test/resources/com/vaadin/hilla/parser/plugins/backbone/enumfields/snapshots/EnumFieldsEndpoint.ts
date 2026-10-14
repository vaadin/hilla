import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Level from './com/vaadin/hilla/parser/plugins/backbone/enumfields/EnumFieldsEndpoint/Level.js';
import client from './connect-client.default.js';

async function getLevel(init?: EndpointRequestInit): Promise<Level | undefined> {
  return client.call('EnumFieldsEndpoint', 'getLevel', {}, init);
}

export { getLevel };
