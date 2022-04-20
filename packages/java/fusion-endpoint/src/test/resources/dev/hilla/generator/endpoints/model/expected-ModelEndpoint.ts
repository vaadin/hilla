/**
 * This module is generated from ModelEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ModelEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { Subscription } from '@hilla/frontend';
import type Account from './dev/hilla/generator/endpoints/model/ModelEndpoint/Account';
import type Group from './dev/hilla/generator/endpoints/model/ModelEndpoint/Group';
import type ModelFromDifferentPackage from './dev/hilla/generator/endpoints/model/subpackage/ModelFromDifferentPackage';

function _getAccountByGroups(
  groups: Array<Group | undefined> | undefined
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByGroups', {groups});
}

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

function _getArrayOfAccount(): Promise<Array<Account | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getArrayOfAccount');
}

function _getMapGroups(): Promise<Record<string, Group | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getMapGroups');
}

/**
 * The import path of this model should be correct.
 *
 *
 */
function _getModelFromDifferentPackage(): Promise<ModelFromDifferentPackage | undefined> {
  return client.call('ModelEndpoint', 'getModelFromDifferentPackage');
}

export {
  _getAccountByGroups as getAccountByGroups,
  _getAccountByUserName as getAccountByUserName,
  _getArrayOfAccount as getArrayOfAccount,
  _getMapGroups as getMapGroups,
  _getModelFromDifferentPackage as getModelFromDifferentPackage,
};
