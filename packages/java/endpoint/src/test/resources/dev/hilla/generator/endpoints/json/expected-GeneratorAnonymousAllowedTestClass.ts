/**
 * This nested class is also used in the OpenApi generator test
 *
 * This module is generated from GeneratorAnonymousAllowedTestClass.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 * @module GeneratorAnonymousAllowedTestClass
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _anonymousAllowed(__init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'anonymousAllowed', {}, __init);
}

function _permissionAltered1(__init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered1', {}, __init);
}

function _permissionAltered2(__init?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered2', {}, __init);
}

export {
  _anonymousAllowed as anonymousAllowed,
  _permissionAltered1 as permissionAltered1,
  _permissionAltered2 as permissionAltered2,
};
