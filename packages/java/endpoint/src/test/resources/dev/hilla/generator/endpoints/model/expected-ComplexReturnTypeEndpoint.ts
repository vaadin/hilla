/**
 * This module is generated from ComplexReturnTypeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexReturnTypeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Account from './dev/hilla/generator/endpoints/model/ModelEndpoint/Account';

function _getAccounts(__init?: EndpointRequestInit): Promise<Array<Account | undefined> | undefined> {
  return client.call('ComplexReturnTypeEndpoint', 'getAccounts', {}, __init);
}

export {
  _getAccounts as getAccounts,
};
