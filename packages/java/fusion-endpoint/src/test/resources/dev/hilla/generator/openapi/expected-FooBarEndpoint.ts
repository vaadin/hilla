/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from FooBarEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module FooBarEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { Subscription } from '@hilla/frontend';

function _firstMethod(
  value?: boolean
): Promise<void> {
  return client.call('FooBarEndpoint', 'firstMethod', {value}, {requireCredentials: false});
}

export {
  _firstMethod as firstMethod,
};
