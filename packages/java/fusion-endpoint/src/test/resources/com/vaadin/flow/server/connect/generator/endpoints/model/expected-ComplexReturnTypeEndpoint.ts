/**
 * This module is generated from ComplexReturnTypeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexReturnTypeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/flow/server/connect/generator/endpoints/model/ModelEndpoint/Account';

function _getAccounts(): Promise<Array<Account> | undefined> {
  return client.call('ComplexReturnTypeEndpoint', 'getAccounts');
}
export {_getAccounts as getAccounts};

export const ComplexReturnTypeEndpoint = Object.freeze({
  getAccounts: _getAccounts,
});