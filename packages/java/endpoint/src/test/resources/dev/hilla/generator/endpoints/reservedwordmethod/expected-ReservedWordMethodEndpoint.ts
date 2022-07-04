/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from ReservedWordMethodEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ReservedWordMethodEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _delete(__init?: EndpointRequestInit): Promise<void> {
  return client.call('ReservedWordMethodEndpoint', 'delete', {}, __init);
}

export {
  _delete as delete,
};
