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
  __init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByGroups', {groups}, __init);
}

/**
 * Get account by username.
 *
 * @param userName username of the account
 * @param __init an optional object containing additional parameters for the request
 * Return the account with given userName
 */
function _getAccountByUserName(
  userName: string | undefined,
  __init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('ModelEndpoint', 'getAccountByUserName', {userName}, __init);
}

function _getArrayOfAccount (__init?: EndpointRequestInit): Promise<Array<Account | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getArrayOfAccount', {}, __init);
}

function _getMapGroups(__init?: EndpointRequestInit): Promise<Record<string, Group | undefined> | undefined> {
  return client.call('ModelEndpoint', 'getMapGroups', {}, __init);
}

/**
 * The import path of this model should be correct.
 *
 * @param __init an optional object containing additional parameters for the request
 *
 */
function _getModelFromDifferentPackage(__init?: EndpointRequestInit): Promise<ModelFromDifferentPackage | undefined> {
  return client.call('ModelEndpoint', 'getModelFromDifferentPackage', {}, __init);
}

export {
  _getAccountByGroups as getAccountByGroups,
  _getAccountByUserName as getAccountByUserName,
  _getArrayOfAccount as getArrayOfAccount,
  _getMapGroups as getMapGroups,
  _getModelFromDifferentPackage as getModelFromDifferentPackage,
};
