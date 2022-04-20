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
import { Subscription } from '@hilla/frontend';
import type Version from './com/fasterxml/jackson/core/Version';
import type Status from './dev/hilla/generator/endpoints/json/JsonTestEndpoint/Status';
import type User from './dev/hilla/generator/endpoints/json/JsonTestEndpoint/User';

/**
 * Get number of users
 *
 * Return number of user
 */
function _countUser(): Promise<number> {
  return client.call('JsonTestEndpoint', 'countUser');
}

/**
 * Get instant nano
 *
 * @param input input parameter
 * Return current time as an Instant
 */
function _fullFQNMethod(
  input: number | undefined
): Promise<string | undefined> {
  return client.call('JsonTestEndpoint', 'fullFQNMethod', {input});
}

/**
 * Get the map of user and roles
 *
 * Return map of user and roles
 */
function _getAllUserRolesMap(): Promise<Record<string, User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUserRolesMap');
}

/**
 * Get all users
 *
 * Return list of users
 */
function _getAllUsers(): Promise<Array<User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUsers');
}

/**
 * Get array int
 *
 * @param input input string array
 * Return array of int
 */
function _getArrayInt(
  input: Array<string | undefined> | undefined
): Promise<Array<number> | undefined> {
  return client.call('JsonTestEndpoint', 'getArrayInt', {input});
}

/**
 * Get boolean value
 *
 * @param input input map
 * Return boolean value
 */
function _getBooleanValue(
  input: Record<string, User | undefined> | undefined
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getBooleanValue', {input});
}

/**
 * Two parameters input method
 *
 * @param input first input description
 * @param secondInput second input description
 * Return boolean value
 */
function _getTwoParameters(
  input: string | undefined,
  secondInput: number
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getTwoParameters', {input, secondInput});
}

/**
 * Get user by id
 *
 * @param id id of user
 * Return user with given id
 */
function _getUserById(
  id: number
): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'getUserById', {id});
}

function _inputBeanTypeDependency(
  input: Version | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeDependency', {input});
}

function _inputBeanTypeLocal(
  input: Status | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeLocal', {input});
}

function _optionalParameter(
  parameter: Array<string | undefined> | undefined,
  requiredParameter: string | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'optionalParameter', {parameter, requiredParameter});
}

function _optionalReturn(): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'optionalReturn');
}

function _reservedWordInParameter(
  _delete: boolean
): Promise<void> {
  return client.call('JsonTestEndpoint', 'reservedWordInParameter', {_delete});
}

/**
 * Update a user
 *
 * @param user User to be updated
 *
 */
function _updateUser(
  user: User | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'updateUser', {user});
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
