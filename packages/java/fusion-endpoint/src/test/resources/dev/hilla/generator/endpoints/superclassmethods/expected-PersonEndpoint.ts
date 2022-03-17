/**
 * This module is generated from PersonEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module PersonEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { Subscription } from '@hilla/frontend';
import type Person from './dev/hilla/generator/endpoints/superclassmethods/PersonEndpoint/Person';

function _delete(
  id: number | undefined
): Promise<void> {
  return client.call('PersonEndpoint', 'delete', {id});
}

function _getNonNullablePage(
  pageSize: number,
  pageNumber: number,
  parameters: Record<string, Person> | undefined
): Promise<Array<Person> | undefined> {
  return client.call('PersonEndpoint', 'getNonNullablePage', {pageSize, pageNumber, parameters});
}

function _get(
  id: number | undefined
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'get', {id});
}

function _getPage(
  pageSize: number,
  pageNumber: number
): Promise<Array<Person | undefined> | undefined> {
  return client.call('PersonEndpoint', 'getPage', {pageSize, pageNumber});
}

function _size(): Promise<number> {
  return client.call('PersonEndpoint', 'size');
}

function _update(
  entity: Person | undefined
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'update', {entity});
}

export {
  _delete as delete,
  _getNonNullablePage as getNonNullablePage,
  _get as get,
  _getPage as getPage,
  _size as size,
  _update as update,
};
