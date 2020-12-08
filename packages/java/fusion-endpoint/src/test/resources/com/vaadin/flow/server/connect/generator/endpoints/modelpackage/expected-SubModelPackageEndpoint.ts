/**
 * This module is generated from SubModelPackageEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SubModelPackageEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/flow/server/connect/generator/endpoints/modelpackage/subpackage/Account';

function _getSubAccountPackage(
  name: string
): Promise<Account> {
  return client.call('SubModelPackageEndpoint', 'getSubAccountPackage', {name});
}
export {_getSubAccountPackage as getSubAccountPackage};