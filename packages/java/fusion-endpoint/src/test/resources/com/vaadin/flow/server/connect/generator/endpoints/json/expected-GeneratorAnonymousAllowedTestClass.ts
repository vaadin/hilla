/**
 * This nested class is also used in the OpenApi generator test
 *
 * This module is generated from GeneratorAnonymousAllowedTestClass.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module GeneratorAnonymousAllowedTestClass
 */

// @ts-ignore
import client from './connect-client.default';

function _anonymousAllowed(): Promise<void> {
  return client.call('customName', 'anonymousAllowed');
}
export {_anonymousAllowed as anonymousAllowed};

function _permissionAltered1(): Promise<void> {
  return client.call('customName', 'permissionAltered1');
}
export {_permissionAltered1 as permissionAltered1};

function _permissionAltered2(): Promise<void> {
  return client.call('customName', 'permissionAltered2');
}
export {_permissionAltered2 as permissionAltered2};