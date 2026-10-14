import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Box from './com/vaadin/hilla/parser/plugins/nonnull/genericentity/GenericEntityEndpoint/Box.js';
import type Holder from './com/vaadin/hilla/parser/plugins/nonnull/genericentity/GenericEntityEndpoint/Holder.js';
import client from './connect-client.default.js';

export async function boxOfMaybe(init?: EndpointRequestInit): Promise<Box<string | undefined>> {
  return client.call('GenericEntityEndpoint', 'boxOfMaybe', {}, init);
}

export async function getHolder(init?: EndpointRequestInit): Promise<Holder | undefined> {
  return client.call('GenericEntityEndpoint', 'getHolder', {}, init);
}

export async function maybeBoxOf(init?: EndpointRequestInit): Promise<Box<string> | undefined> {
  return client.call('GenericEntityEndpoint', 'maybeBoxOf', {}, init);
}
