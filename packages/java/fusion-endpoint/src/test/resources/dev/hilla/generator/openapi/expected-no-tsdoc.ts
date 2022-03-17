/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from GeneratorTestClass.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module GeneratorTestClass
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { Subscription } from '@hilla/frontend';

function _getAllUsers(): Promise<void> {
  return client.call('GeneratorTestClass', 'getAllUsers', undefined, {requireCredentials: false});
}

export {
  _getAllUsers as getAllUsers,
};
