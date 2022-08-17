/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from FluxTestEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module FluxTestEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type User from './dev/hilla/generator/endpoints/flux/FluxTestEndpoint/User';
function _getAllUsersNonnull():
Subscription<User> {
  return client.subscribe (
    'FluxTestEndpoint', 'getAllUsersNonnull',{}
  );
}
/**
* Get all users
*
* Return a list of users
*/
function _getAllUsers():
Subscription<User | undefined> {
  return client.subscribe (
    'FluxTestEndpoint', 'getAllUsers',{}
  );
}
export {
  _getAllUsersNonnull as getAllUsersNonnull,
  _getAllUsers as getAllUsers,
};