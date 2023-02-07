/**
 * This module is generated from IterableEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 * @module IterableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Foo from './dev/hilla/generator/endpoints/iterableendpoint/IterableEndpoint/Foo';

function _getFoos(__init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFoos', {}, __init);
}

export {
  _getFoos as getFoos,
};
