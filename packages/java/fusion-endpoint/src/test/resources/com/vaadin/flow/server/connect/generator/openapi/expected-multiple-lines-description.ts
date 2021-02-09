/**
 * This class is used
 * <h1>for OpenApi generator test</h1>
 *
 * This module is generated from GeneratorTestClass.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module GeneratorTestClass
 */

// @ts-ignore
import client from './connect-client.default';
import User from './User';

/**
 * Get all users
 *
 * Return list of users
 */
function _getAllUsers(): Promise<Array<User>> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
export {_getAllUsers as getAllUsers};

export const GeneratorTestClass = Object.freeze({
  getAllUsers: _getAllUsers,
});