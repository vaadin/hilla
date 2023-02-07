/**
 * This module is generated from SelfReferenceEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 * @module SelfReferenceEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type SelfReference from './dev/hilla/generator/endpoints/selfreference/SelfReference';

function _getModel(__init?: EndpointRequestInit): Promise<SelfReference | undefined> {
  return client.call('SelfReferenceEndpoint', 'getModel', {}, __init);
}
export {
  _getModel as getModel,
};
