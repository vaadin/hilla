/**
 * This nested class is also used in the OpenApi generator test
 *
 * This module is generated from GeneratorAnonymousAllowedTestClass.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module GeneratorAnonymousAllowedTestClass
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _anonymousAllowed(init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'anonymousAllowed', {}, init);
}

function _permissionAltered1(init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered1', {}, init);
}

function _permissionAltered2(init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered2', {}, init);
}

export {
  _anonymousAllowed as anonymousAllowed,
  _permissionAltered1 as permissionAltered1,
  _permissionAltered2 as permissionAltered2,
};
