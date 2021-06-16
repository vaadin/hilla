/**
 * This module is generated from ModelEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ModelEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/flow/server/connect/generator/endpoints/model/ModelEndpoint/Account';
import Group from './com/vaadin/flow/server/connect/generator/endpoints/model/ModelEndpoint/Group';
import ModelFromDifferentPackage from './com/vaadin/flow/server/connect/generator/endpoints/model/subpackage/ModelFromDifferentPackage';

function _getAccountByGroups(
    groups: Array<Group | undefined> | undefined
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByGroups', {groups});
}
export {_getAccountByGroups as getAccountByGroups};

/**
 * Get account by username.
 *
 * @param userName username of the account
 * Return the account with given userName
 */
function _getAccountByUserName(
    userName: string | undefined
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByUserName', {userName});
}
export {_getAccountByUserName as getAccountByUserName};

function _getArrayOfAccount(): Promise<Array<Account | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getArrayOfAccount');
}
export {_getArrayOfAccount as getArrayOfAccount};

function _getMapGroups(): Promise<Record<string, Group | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getMapGroups');
}
export {_getMapGroups as getMapGroups};

/**
 * The import path of this model should be correct.
 *
 *
 */
function _getModelFromDifferentPackage(): Promise<ModelFromDifferentPackage | undefined> {
  return client.call('ModelEndpoint', 'getModelFromDifferentPackage');
}
export {_getModelFromDifferentPackage as getModelFromDifferentPackage};

export const ModelEndpoint = Object.freeze({
  getAccountByGroups: _getAccountByGroups,
  getAccountByUserName: _getAccountByUserName,
  getArrayOfAccount: _getArrayOfAccount,
  getMapGroups: _getMapGroups,
  getModelFromDifferentPackage: _getModelFromDifferentPackage,
});