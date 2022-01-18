/**
 * This module is generated from DenyAllEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module DenyAllEndpoint
 */

// @ts-ignore
import client from './connect-client.default';

function _shouldBeDisplayed1(): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed1');
}

function _shouldBeDisplayed2(): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed2');
}

function _shouldBeDisplayed3(): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed3');
}

export{
  _shouldBeDisplayed1 as shouldBeDisplayed1,
  _shouldBeDisplayed2 as shouldBeDisplayed2,
  _shouldBeDisplayed3 as shouldBeDisplayed3,
};
