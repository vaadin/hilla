import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Holder from './com/vaadin/hilla/parser/plugins/backbone/boundedmethod/BoundedMethodEndpoint/Holder.js';
import type Item from './com/vaadin/hilla/parser/plugins/backbone/boundedmethod/BoundedMethodEndpoint/Item.js';
import client from './connect-client.default.js';

export async function first(
  items: Array<Item | undefined> | undefined,
  init?: EndpointRequestInit,
): Promise<Item | undefined> {
  return client.call('BoundedMethodEndpoint', 'first', { items }, init);
}

export async function getHolder(init?: EndpointRequestInit): Promise<Holder<string | undefined> | undefined> {
  return client.call('BoundedMethodEndpoint', 'getHolder', {}, init);
}
