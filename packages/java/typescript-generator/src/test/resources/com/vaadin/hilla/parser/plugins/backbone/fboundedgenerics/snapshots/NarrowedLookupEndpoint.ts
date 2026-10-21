import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type EnumLookupValue from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/EnumLookupValue.js';
import type NarrowedLookup from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NarrowedLookup.js';
import type NodeType from './com/vaadin/hilla/parser/plugins/backbone/fboundedgenerics/NodeType.js';
import client from './connect-client.default.js';

async function getNarrowedLookup(init?: EndpointRequestInit): Promise<NarrowedLookup<EnumLookupValue<NodeType | undefined> | undefined> | undefined> {
  return client.call('NarrowedLookupEndpoint', 'getNarrowedLookup', {}, init);
}

export { getNarrowedLookup };
