/**
 * This module is generated from ModelPackageEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ModelPackageEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/flow/server/connect/generator/endpoints/modelpackage/ModelPackageEndpoint/Account';

/**
 * Get a list of user name.
 *
 * Return list of user name
 */
function _getListOfUserName(): Promise<Array<string>> {
  return client.call('ModelPackageEndpoint', 'getListOfUserName');
}
export {_getListOfUserName as getListOfUserName};

/**
 * Get a collection by author name. The generator should not mix this type with the Java's Collection type.
 *
 * @param name author name
 * Return a collection
 */
function _getSameModelPackage(
    name: string
): Promise<Account> {
  return client.call('ModelPackageEndpoint', 'getSameModelPackage', {name});
}
export {_getSameModelPackage as getSameModelPackage};

export const ModelPackageEndpoint = Object.freeze({
  getListOfUserName: _getListOfUserName,
  getSameModelPackage: _getSameModelPackage,
});