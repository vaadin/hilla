/**
 * This module is generated from ComplexReturnTypeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexReturnTypeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type Account from './com/vaadin/fusion/generator/endpoints/model/ModelEndpoint/Account';

function _getAccounts(): Promise<Array<Account | undefined> | undefined> {
  return client.call('ComplexReturnTypeEndpoint', 'getAccounts');
}

export {
  _getAccounts as getAccounts,
};
