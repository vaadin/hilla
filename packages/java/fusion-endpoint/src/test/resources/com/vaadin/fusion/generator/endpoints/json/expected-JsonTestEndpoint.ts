/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from JsonTestEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module JsonTestEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Version from './com/fasterxml/jackson/core/Version';
import Status from './com/vaadin/fusion/generator/endpoints/json/JsonTestEndpoint/Status';
import User from './com/vaadin/fusion/generator/endpoints/json/JsonTestEndpoint/User';

/**
 * Get number of users
 *
 * Return number of user
 */
function _countUser(): Promise<number> {
  return client.call('JsonTestEndpoint', 'countUser');
}
export {_countUser as countUser};

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
export {_fullFQNMethod as fullFQNMethod};

/**
 * Get the map of user and roles
 *
 * Return map of user and roles
 */
function _getAllUserRolesMap(): Promise<Readonly<Record<string, User | undefined>> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUserRolesMap');
}
export {_getAllUserRolesMap as getAllUserRolesMap};

/**
 * Get all users
 *
 * Return list of users
 */
function _getAllUsers(): Promise<ReadonlyArray<User | undefined> | undefined> {
  return client.call('JsonTestEndpoint', 'getAllUsers');
}
export {_getAllUsers as getAllUsers};

/**
 * Get array int
 *
 * @param input input string array
 * Return array of int
 */
function _getArrayInt(
    input: ReadonlyArray<string | undefined> | undefined
): Promise<ReadonlyArray<number> | undefined> {
  return client.call('JsonTestEndpoint', 'getArrayInt', {input});
}
export {_getArrayInt as getArrayInt};

/**
 * Get boolean value
 *
 * @param input input map
 * Return boolean value
 */
function _getBooleanValue(
    input: Readonly<Record<string, User | undefined>> | undefined
): Promise<boolean> {
  return client.call('JsonTestEndpoint', 'getBooleanValue', {input});
}
export {_getBooleanValue as getBooleanValue};

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
export {_getTwoParameters as getTwoParameters};

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
export {_getUserById as getUserById};

function _inputBeanTypeDependency(
    input: Version | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeDependency', {input});
}
export {_inputBeanTypeDependency as inputBeanTypeDependency};

function _inputBeanTypeLocal(
    input: Status | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'inputBeanTypeLocal', {input});
}
export {_inputBeanTypeLocal as inputBeanTypeLocal};

function _optionalParameter(
    parameter: ReadonlyArray<string | undefined> | undefined,
    requiredParameter: string | undefined
): Promise<void> {
  return client.call('JsonTestEndpoint', 'optionalParameter', {parameter, requiredParameter});
}
export {_optionalParameter as optionalParameter};

function _optionalReturn(): Promise<User | undefined> {
  return client.call('JsonTestEndpoint', 'optionalReturn');
}
export {_optionalReturn as optionalReturn};

function _reservedWordInParameter(
    _delete: boolean
): Promise<void> {
  return client.call('JsonTestEndpoint', 'reservedWordInParameter', {_delete});
}
export {_reservedWordInParameter as reservedWordInParameter};

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
export {_updateUser as updateUser};

export const JsonTestEndpoint = Object.freeze({
  countUser: _countUser,
  fullFQNMethod: _fullFQNMethod,
  getAllUserRolesMap: _getAllUserRolesMap,
  getAllUsers: _getAllUsers,
  getArrayInt: _getArrayInt,
  getBooleanValue: _getBooleanValue,
  getTwoParameters: _getTwoParameters,
  getUserById: _getUserById,
  inputBeanTypeDependency: _inputBeanTypeDependency,
  inputBeanTypeLocal: _inputBeanTypeLocal,
  optionalParameter: _optionalParameter,
  optionalReturn: _optionalReturn,
  reservedWordInParameter: _reservedWordInParameter,
  updateUser: _updateUser,
});