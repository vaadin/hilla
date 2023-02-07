/**
 * This module is generated from ModelPackageEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 * @module ModelPackageEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Account from './dev/hilla/generator/endpoints/modelpackage/ModelPackageEndpoint/Account';

/**
 * Get a list of user name.
 *
 * @param __init an optional object containing additional parameters for the request
 * Return list of user name
 */
function _getListOfUserName(__init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
  return client.call('ModelPackageEndpoint', 'getListOfUserName', {}, __init);
}

/**
 * Get a collection by author name. The generator should not mix this type with the Java's Collection type.
 *
 * @param name author name
 * @param __init an optional object containing additional parameters for the request
 * Return a collection
 */
function _getSameModelPackage(
  name: string | undefined,
  __init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('ModelPackageEndpoint', 'getSameModelPackage', {name}, __init);
}

export {
  _getListOfUserName as getListOfUserName,
  _getSameModelPackage as getSameModelPackage,
};
