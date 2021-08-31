/**
* This module is generated from MappedTypeEndpoint.java
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
* @module MappedTypeEndpoint
*/
// @ts-ignore
import client from './connect-client.default';
import type PageableDTO from './com/vaadin/fusion/endpointransfermapper/PageableDTO';
function _getPageable(): Promise<PageableDTO | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'getPageable'
  );
}
function _parameter (
  pageable: PageableDTO | undefined
): Promise<void> {
  return client.call (
    'MappedTypeEndpoint', 'parameter', {
      pageable
    }
  );
}
function _returnValue(): Promise<PageableDTO | undefined> {
  return client.call (
    'MappedTypeEndpoint', 'returnValue'
  );
}
export {
  _getPageable as getPageable,
  _parameter as parameter,
  _returnValue as returnValue,
};