import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type EnumLookupValue from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/EnumLookupValue.js';
import type MutuallyBoundedPair from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/MutuallyBoundedPair.js';
import type NodeType from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NodeType.js';
import type WildcardBounded from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/WildcardBounded.js';
import client from './connect-client.default.js';

async function getNodeType(init?: EndpointRequestInit): Promise<EnumLookupValue<NodeType | undefined> | undefined> {
  return client.call('FBoundedGenericsEndpoint', 'getNodeType', {}, init);
}

async function getPair(init?: EndpointRequestInit): Promise<MutuallyBoundedPair<string | undefined, string | undefined> | undefined> {
  return client.call('FBoundedGenericsEndpoint', 'getPair', {}, init);
}

async function getWildcardBounded(init?: EndpointRequestInit): Promise<WildcardBounded<string | undefined> | undefined> {
  return client.call('FBoundedGenericsEndpoint', 'getWildcardBounded', {}, init);
}

export { getNodeType, getPair, getWildcardBounded };
