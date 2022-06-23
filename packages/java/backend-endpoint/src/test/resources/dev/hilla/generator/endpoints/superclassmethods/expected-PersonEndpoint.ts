/**
 * This module is generated from PersonEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module PersonEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type Person from './dev/hilla/generator/endpoints/superclassmethods/PersonEndpoint/Person';

function _delete(
  id: number | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('PersonEndpoint', 'delete', {id}, init);
}

function _getNonNullablePage(
  pageSize: number,
  pageNumber: number,
  parameters: Record<string, Person> | undefined,
  init?: EndpointRequestInit
): Promise<Array<Person> | undefined> {
  return client.call('PersonEndpoint', 'getNonNullablePage', {pageSize, pageNumber, parameters}, init);
}

function _get(
  id: number | undefined,
  init?: EndpointRequestInit
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'get', {id}, init);
}

function _getPage(
  pageSize: number,
  pageNumber: number,
  init?: EndpointRequestInit
): Promise<Array<Person | undefined> | undefined> {
  return client.call('PersonEndpoint', 'getPage', {pageSize, pageNumber}, init);
}

function _size(init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'size', {}, init);
}

function _update (
  entity: Person | undefined,
  init?: EndpointRequestInit
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'update', {entity}, init);
}

export {
  _delete as delete,
  _getNonNullablePage as getNonNullablePage,
  _get as get,
  _getPage as getPage,
  _size as size,
  _update as update,
};
