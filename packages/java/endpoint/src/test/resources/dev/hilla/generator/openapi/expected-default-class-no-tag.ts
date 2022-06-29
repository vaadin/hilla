/**
 * This module is generated from Default.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module Default
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type User from './User';

/**
 * Get all users
 *
 * @param __init an optional object containing additional parameters for the request
 * Return list of users
 */
function _getAllUsers(__init?: EndpointRequestInit): Promise<Array<User | undefined>> {
  return client.call('GeneratorTestClass', 'getAllUsers', {}, __init);
}

export {
  _getAllUsers as getAllUsers,
};
