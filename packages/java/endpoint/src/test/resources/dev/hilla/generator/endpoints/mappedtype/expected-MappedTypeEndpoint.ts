/**
* This module is generated from MappedTypeEndpoint.java
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
* @module MappedTypeEndpoint
*/
// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Pageable from './dev/hilla/mappedtypes/Pageable';
function _getPageable(endpointRequestInit?: EndpointRequestInit): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'getPageable', {}, endpointRequestInit
  );
}
function _parameter (
  pageable: Pageable | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call (
    'MappedTypeEndpoint', 'parameter', {
      pageable
    }, endpointRequestInit
  );
}
function _returnValue(endpointRequestInit?: EndpointRequestInit): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'returnValue', {}, endpointRequestInit
  );
}
export {
  _getPageable as getPageable,
  _parameter as parameter,
  _returnValue as returnValue,
};
