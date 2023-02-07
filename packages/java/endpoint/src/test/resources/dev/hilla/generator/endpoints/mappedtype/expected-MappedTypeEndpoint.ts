/**
* This module is generated from MappedTypeEndpoint.java
* All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
* @module MappedTypeEndpoint
*/
// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Pageable from './dev/hilla/mappedtypes/Pageable';
function _getPageable(__init?: EndpointRequestInit): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'getPageable', {}, __init
  );
}
function _parameter (
  pageable: Pageable | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call (
    'MappedTypeEndpoint', 'parameter', {
      pageable
    }, __init
  );
}
function _returnValue(__init?: EndpointRequestInit): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'returnValue', {}, __init
  );
}
export {
  _getPageable as getPageable,
  _parameter as parameter,
  _returnValue as returnValue,
};
