import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type MinimalBase from './com/vaadin/hilla/parser/plugins/subtypes/typeids/MinimalBase.js';
import type NameBase from './com/vaadin/hilla/parser/plugins/subtypes/typeids/NameBase.js';
import type SimpleBase from './com/vaadin/hilla/parser/plugins/subtypes/typeids/SimpleBase.js';
import client from './connect-client.default.js';

export async function sendMinimal(init?: EndpointRequestInit): Promise<MinimalBase | undefined> {
  return client.call('TypeIdsEndpoint', 'sendMinimal', {}, init);
}

export async function sendNamed(init?: EndpointRequestInit): Promise<NameBase | undefined> {
  return client.call('TypeIdsEndpoint', 'sendNamed', {}, init);
}

export async function sendSimple(init?: EndpointRequestInit): Promise<SimpleBase | undefined> {
  return client.call('TypeIdsEndpoint', 'sendSimple', {}, init);
}
