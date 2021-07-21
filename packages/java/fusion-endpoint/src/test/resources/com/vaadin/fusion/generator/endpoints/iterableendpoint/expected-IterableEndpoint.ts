/**
 * This module is generated from IterableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module IterableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type Foo from './com/vaadin/fusion/generator/endpoints/iterableendpoint/IterableEndpoint/Foo';

function _getFoos(): Promise<ReadonlyArray<Foo | undefined> | undefined> {
  return client.call('IterableEndpoint', 'getFoos');
}

export {
  _getFoos as getFoos,
};
