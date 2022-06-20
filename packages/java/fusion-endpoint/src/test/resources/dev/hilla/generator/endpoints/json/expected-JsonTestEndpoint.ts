/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from JsonTestEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module JsonTestEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Version from './com/fasterxml/jackson/core/Version';
import type Status from './dev/hilla/generator/endpoints/json/JsonTestEndpoint/Status';
import type User from './dev/hilla/generator/endpoints/json/JsonTestEndpoint/User';

/**
 * Get number of users
 *
 * @param init an optional object containing additional parameters for the request
 * Return number of user
 */
function _countUser(init?: EndpointRequestInit): Promise<number> {
  return client.call('JsonTestEndpoint', 'countUser', {}, init);
}

/**
 * Get instant nano
 *
 * @param input input parameter
 * @param init an optional object containing additional parameters for the request
 * Return current time as an Instant
 */
function _fullFQNMethod(
  input: number | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('JsonTestEndpoint', 'fullFQNMethod', {input}, init);
}

/**
 * Get the map of user and roles
 *
 * @param init an optional object containing additional parameters for the request
 * Return map of user and roles
 */
function _getAllUserRolesMap(init?: EndpointRequestInit): Promise<Record<string, User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUserRolesMap', {}, init);
}

/**
 * Get all users
 *
 * @param init an optional object containing additional parameters for the request
 * Return list of users
 */
function _getAllUsers(init?: EndpointRequestInit): Promise<Array<User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUsers', {}, init);
}

/**
 * Get array int
 *
 * @param input input string array
 * @param init an optional object containing additional parameters for the request
 * Return array of int
 */
function _getArrayInt(
  input: Array<string | undefined> | undefined,
  init?: EndpointRequestInit
): Promise<Array<number> | undefined> {
  return client.call('JsonTestEndpoint', 'getArrayInt', {input}, init);
}

/**
 * Get boolean value
 *
 * @param input input map
 * @param init an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getBooleanValue(
  input: Record<string, User | undefined> | undefined,
  init?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getBooleanValue', {input}, init);
}

/**
 * Two parameters input method
 *
 * @param input first input description
 * @param secondInput second input description
 * @param init an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getTwoParameters(
  input: string | undefined,
  secondInput: number,
  init?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getTwoParameters', {input, secondInput}, init);
}

/**
 * Get user by id
 *
 * @param id id of user
 * @param init an optional object containing additional parameters for the request
 * Return user with given id
 */
function _getUserById(
  id: number,
  init?: EndpointRequestInit
): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'getUserById', {id}, init);
}

function _inputBeanTypeDependency(
  input: Version | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call ('JsonTestEndpoint', 'inputBeanTypeDependency', {input}, init);
}

function _inputBeanTypeLocal(
  input: Status | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeLocal', {input}, init);
}

function _optionalParameter(
  parameter: Array<string | undefined> | undefined,
  requiredParameter: string | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'optionalParameter', {parameter, requiredParameter}, init);
}

function _optionalReturn(init?: EndpointRequestInit): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'optionalReturn', {}, init);
}

function _reservedWordInParameter(
  _delete: boolean,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'reservedWordInParameter', {_delete}, init);
}

/**
 * Update a user
 *
 * @param user User to be updated
 * @param init an optional object containing additional parameters for the request
 *
 */
function _updateUser(
  user: User | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'updateUser', {user}, init);
}

export {
  _countUser as countUser,
  _fullFQNMethod as fullFQNMethod,
  _getAllUserRolesMap as getAllUserRolesMap,
  _getAllUsers as getAllUsers,
  _getArrayInt as getArrayInt,
  _getBooleanValue as getBooleanValue,
  _getTwoParameters as getTwoParameters,
  _getUserById as getUserById,
  _inputBeanTypeDependency as inputBeanTypeDependency,
  _inputBeanTypeLocal as inputBeanTypeLocal,
  _optionalParameter as optionalParameter,
  _optionalReturn as optionalReturn,
  _reservedWordInParameter as reservedWordInParameter,
  _updateUser as updateUser,
};
