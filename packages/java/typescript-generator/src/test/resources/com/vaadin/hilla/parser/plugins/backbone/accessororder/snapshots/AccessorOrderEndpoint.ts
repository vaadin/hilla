import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Indexed from './com/vaadin/hilla/parser/plugins/backbone/accessororder/AccessorOrderEndpoint/Indexed.js';
import type Ordered from './com/vaadin/hilla/parser/plugins/backbone/accessororder/AccessorOrderEndpoint/Ordered.js';
import type PartlyOrdered from './com/vaadin/hilla/parser/plugins/backbone/accessororder/AccessorOrderEndpoint/PartlyOrdered.js';
import type Range from './com/vaadin/hilla/parser/plugins/backbone/accessororder/AccessorOrderEndpoint/Range.js';
import client from './connect-client.default.js';

async function get(init?: EndpointRequestInit): Promise<Ordered | undefined> {
  return client.call('AccessorOrderEndpoint', 'get', {}, init);
}

async function getIndexed(init?: EndpointRequestInit): Promise<Indexed | undefined> {
  return client.call('AccessorOrderEndpoint', 'getIndexed', {}, init);
}

async function getPartlyOrdered(init?: EndpointRequestInit): Promise<PartlyOrdered | undefined> {
  return client.call('AccessorOrderEndpoint', 'getPartlyOrdered', {}, init);
}

async function getRange(init?: EndpointRequestInit): Promise<Range | undefined> {
  return client.call('AccessorOrderEndpoint', 'getRange', {}, init);
}

export { get, getIndexed, getPartlyOrdered, getRange };
