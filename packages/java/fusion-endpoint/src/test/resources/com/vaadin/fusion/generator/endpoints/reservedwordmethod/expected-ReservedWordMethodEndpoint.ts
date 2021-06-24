/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from ReservedWordMethodEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ReservedWordMethodEndpoint
 */

// @ts-ignore
import client from './connect-client.default';

function _delete(): Promise<void> {
  return client.call('ReservedWordMethodEndpoint', 'delete');
}
export {_delete as delete};

export const ReservedWordMethodEndpoint = Object.freeze({
  delete: _delete,
});