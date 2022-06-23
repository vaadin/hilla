/**
 * This module is generated from DenyAllEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module DenyAllEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';

function _shouldBeDisplayed1(init?: EndpointRequestInit): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed1', {}, init);
}

function _shouldBeDisplayed2(init?: EndpointRequestInit): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed2', {}, init);
}

function _shouldBeDisplayed3 (init?: EndpointRequestInit): Promise<void> {
  return client.call('DenyAllEndpoint', 'shouldBeDisplayed3', {}, init);
}

export{
  _shouldBeDisplayed1 as shouldBeDisplayed1,
  _shouldBeDisplayed2 as shouldBeDisplayed2,
  _shouldBeDisplayed3 as shouldBeDisplayed3,
};
