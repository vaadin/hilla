/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from JsonTestEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
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
 * @param __init an optional object containing additional parameters for the request
 * Return number of user
 */
function _countUser(__init?: EndpointRequestInit): Promise<number> {
  return client.call('JsonTestEndpoint', 'countUser', {}, __init);
}

/**
 * Get instant nano
 *
 * @param input input parameter
 * @param __init an optional object containing additional parameters for the request
 * Return current time as an Instant
 */
function _fullFQNMethod(
  input: number | undefined,
  __init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call('JsonTestEndpoint', 'fullFQNMethod', {input}, __init);
}

/**
 * Get the map of user and roles
 *
 * @param __init an optional object containing additional parameters for the request
 * Return map of user and roles
 */
function _getAllUserRolesMap(__init?: EndpointRequestInit): Promise<Record<string, User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUserRolesMap', {}, __init);
}

/**
 * Get all users
 *
 * @param __init an optional object containing additional parameters for the request
 * Return list of users
 */
function _getAllUsers(__init?: EndpointRequestInit): Promise<Array<User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUsers', {}, __init);
}

/**
 * Get array int
 *
 * @param input input string array
 * @param __init an optional object containing additional parameters for the request
 * Return array of int
 */
function _getArrayInt(
  input: Array<string | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<Array<number> | undefined> {
  return client.call('JsonTestEndpoint', 'getArrayInt', {input}, __init);
}

/**
 * Get boolean value
 *
 * @param input input map
 * @param __init an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getBooleanValue(
  input: Record<string, User | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getBooleanValue', {input}, __init);
}

/**
 * Two parameters input method
 *
 * @param input first input description
 * @param secondInput second input description
 * @param __init an optional object containing additional parameters for the request
 * Return boolean value
 */
function _getTwoParameters(
  input: string | undefined,
  secondInput: number,
  __init?: EndpointRequestInit
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getTwoParameters', {input, secondInput}, __init);
}

/**
 * Get user by id
 *
 * @param id id of user
 * @param __init an optional object containing additional parameters for the request
 * Return user with given id
 */
function _getUserById(
  id: number,
  __init?: EndpointRequestInit
): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'getUserById', {id}, __init);
}

function _inputBeanTypeDependency(
  input: Version | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call ('JsonTestEndpoint', 'inputBeanTypeDependency', {input}, __init);
}

function _inputBeanTypeLocal(
  input: Status | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeLocal', {input}, __init);
}

function _optionalParameter(
  parameter: Array<string | undefined> | undefined,
  requiredParameter: string | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'optionalParameter', {parameter, requiredParameter}, __init);
}

function _optionalReturn(__init?: EndpointRequestInit): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'optionalReturn', {}, __init);
}

function _reservedWordInParameter(
  _delete: boolean,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'reservedWordInParameter', {_delete}, __init);
}

/**
 * Update a user
 *
 * @param user User to be updated
 * @param __init an optional object containing additional parameters for the request
 *
 */
function _updateUser(
  user: User | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('JsonTestEndpoint', 'updateUser', {user}, __init);
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
