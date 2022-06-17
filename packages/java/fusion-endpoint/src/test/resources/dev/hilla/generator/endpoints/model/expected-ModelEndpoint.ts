/**
 * This module is generated from ModelEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ModelEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Account from './dev/hilla/generator/endpoints/model/ModelEndpoint/Account';
import type Group from './dev/hilla/generator/endpoints/model/ModelEndpoint/Group';
import type ModelFromDifferentPackage from './dev/hilla/generator/endpoints/model/subpackage/ModelFromDifferentPackage';

function _getAccountByGroups(
  groups: Array<Group | undefined> | undefined,
  init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByGroups', {groups}, init);
}

/**
 * Get account by username.
 *
 * @param userName username of the account
 * @param init an optional object containing additional parameters for the request
 * Return the account with given userName
 */
function _getAccountByUserName(
  userName: string | undefined,
  init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByUserName', {userName}, init);
}

function _getArrayOfAccount (init?: EndpointRequestInit): Promise<Array<Account | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getArrayOfAccount', {}, init);
}

function _getMapGroups(init?: EndpointRequestInit): Promise<Record<string, Group | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getMapGroups', {}, init);
}

/**
 * The import path of this model should be correct.
 *
 * @param init an optional object containing additional parameters for the request
 *
 */
function _getModelFromDifferentPackage(init?: EndpointRequestInit): Promise<ModelFromDifferentPackage | undefined> {
  return client.call('ModelEndpoint', 'getModelFromDifferentPackage', {}, init);
}

export {
  _getAccountByGroups as getAccountByGroups,
  _getAccountByUserName as getAccountByUserName,
  _getArrayOfAccount as getArrayOfAccount,
  _getMapGroups as getMapGroups,
  _getModelFromDifferentPackage as getModelFromDifferentPackage,
};
