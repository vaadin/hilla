/**
 * This module is generated from IterableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module IterableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Foo from './com/vaadin/flow/server/connect/generator/endpoints/iterableendpoint/IterableEndpoint/Foo';

function _getFoos(): Promise<Array<Foo | undefined> | undefined> {
    return client.call('IterableEndpoint', 'getFoos');
}
export {_getFoos as getFoos};

export const IterableEndpoint = Object.freeze({
    getFoos: _getFoos,
});