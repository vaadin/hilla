/**
* This module is generated from MappedTypeEndpoint.java
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
* @module MappedTypeEndpoint
*/
// @ts-ignore
import client from './connect-client.default';
import type Pageable from './com/vaadin/fusion/mappedtypes/Pageable';
function _getPageable(): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'getPageable'
  );
}
function _parameter (
  pageable: Pageable | undefined
): Promise<void> {
  return client.call (
    'MappedTypeEndpoint', 'parameter', {
      pageable
    }
  );
}
function _returnValue(): Promise<Pageable | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'returnValue'
  );
}
export {
  _getPageable as getPageable,
  _parameter as parameter,
  _returnValue as returnValue,
};
