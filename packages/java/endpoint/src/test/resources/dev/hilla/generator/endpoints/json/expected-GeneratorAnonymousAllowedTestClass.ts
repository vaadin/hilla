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

function _anonymousAllowed(endpointRequestInit?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'anonymousAllowed', {}, endpointRequestInit);
}

function _permissionAltered1(endpointRequestInit?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered1', {}, endpointRequestInit);
}

function _permissionAltered2(endpointRequestInit?: EndpointRequestInit): Promise<void> {
  return client.call('customName', 'permissionAltered2', {}, endpointRequestInit);
}

export {
  _anonymousAllowed as anonymousAllowed,
  _permissionAltered1 as permissionAltered1,
  _permissionAltered2 as permissionAltered2,
};
