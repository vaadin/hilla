/**
 * This module is generated from SubModelPackageEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SubModelPackageEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Account from './dev/hilla/generator/endpoints/modelpackage/subpackage/Account';

function _getSubAccountPackage(
  name: string | undefined,
  init?: EndpointRequestInit
): Promise<Account | undefined> {
  return client.call('SubModelPackageEndpoint', 'getSubAccountPackage', {name}, init);
}

export {
  _getSubAccountPackage as getSubAccountPackage,
};
