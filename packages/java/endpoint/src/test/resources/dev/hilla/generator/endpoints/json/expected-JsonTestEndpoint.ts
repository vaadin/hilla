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
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return number of user
 */
function _countUser(endpointRequestInit?: EndpointRequestInit): Promise<number> {
  return client.call('JsonTestEndpoint', 'countUser', {}, endpointRequestInit);
}

/**
 * Get instant nano
 *
 * @param input input parameter
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return current time as an Instant
 */
function _fullFQNMethod(
  input: number | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('JsonTestEndpoint', 'fullFQNMethod', {input}, endpointRequestInit);
}

/**
 * Get the map of user and roles
 *
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return map of user and roles
 */
function _getAllUserRolesMap(endpointRequestInit?: EndpointRequestInit): Promise<Record<string, User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUserRolesMap', {}, endpointRequestInit);
}

/**
 * Get all users
 *
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return list of users
 */
function _getAllUsers(endpointRequestInit?: EndpointRequestInit): Promise<Array<User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUsers', {}, endpointRequestInit);
}

/**
 * Get array int
 *
 * @param input input string array
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return array of int
 */
function _getArrayInt(
  input: Array<string | undefined> | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<Array<number> | undefined> {
  return client.call('JsonTestEndpoint', 'getArrayInt', {input}, endpointRequestInit);
}

/**
 * Get boolean value
 *
 * @param input input map
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getBooleanValue(
  input: Record<string, User | undefined> | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getBooleanValue', {input}, endpointRequestInit);
}

/**
 * Two parameters input method
 *
 * @param input first input description
 * @param secondInput second input description
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getTwoParameters(
  input: string | undefined,
  secondInput: number,
  endpointRequestInit?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getTwoParameters', {input, secondInput}, endpointRequestInit);
}

/**
 * Get user by id
 *
 * @param id id of user
 * @param endpointRequestInit an optional object containing additional parameters for the request
 * Return user with given id
 */
function _getUserById(
  id: number,
  endpointRequestInit?: EndpointRequestInit
): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'getUserById', {id}, endpointRequestInit);
}

function _inputBeanTypeDependency(
  input: Version | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call ('JsonTestEndpoint', 'inputBeanTypeDependency', {input}, endpointRequestInit);
}

function _inputBeanTypeLocal(
  input: Status | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeLocal', {input}, endpointRequestInit);
}

function _optionalParameter(
  parameter: Array<string | undefined> | undefined,
  requiredParameter: string | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'optionalParameter', {parameter, requiredParameter}, endpointRequestInit);
}

function _optionalReturn(endpointRequestInit?: EndpointRequestInit): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'optionalReturn', {}, endpointRequestInit);
}

function _reservedWordInParameter(
  _delete: boolean,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'reservedWordInParameter', {_delete}, endpointRequestInit);
}

/**
 * Update a user
 *
 * @param user User to be updated
 * @param endpointRequestInit an optional object containing additional parameters for the request
 *
 */
function _updateUser(
  user: User | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'updateUser', {user}, endpointRequestInit);
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
