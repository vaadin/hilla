/**
 * This module is generated from Default.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module Default
 */

// @ts-ignore
import client from './connect-client.default';
import User from './User';

/**
 * Get all users
 *
 * Return list of users
 */
function _getAllUsers(): Promise<Array<User | undefined>> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}
export {_getAllUsers as getAllUsers};

export const Default = Object.freeze({
  getAllUsers: _getAllUsers,
});