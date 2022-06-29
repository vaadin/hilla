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
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('PersonEndpoint', 'delete', {id}, __init);
}

function _getNonNullablePage(
  pageSize: number,
  pageNumber: number,
  parameters: Record<string, Person> | undefined,
  __init?: EndpointRequestInit
): Promise<Array<Person> | undefined> {
  return client.call('PersonEndpoint', 'getNonNullablePage', {pageSize, pageNumber, parameters}, __init);
}

function _get(
  id: number | undefined,
  __init?: EndpointRequestInit
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'get', {id}, __init);
}

function _getPage(
  pageSize: number,
  pageNumber: number,
  __init?: EndpointRequestInit
): Promise<Array<Person | undefined> | undefined> {
  return client.call('PersonEndpoint', 'getPage', {pageSize, pageNumber}, __init);
}

function _size(__init?: EndpointRequestInit): Promise<number> {
  return client.call('PersonEndpoint', 'size', {}, __init);
}

function _update (
  entity: Person | undefined,
  __init?: EndpointRequestInit
): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'update', {entity}, __init);
}

export {
  _delete as delete,
  _getNonNullablePage as getNonNullablePage,
  _get as get,
  _getPage as getPage,
  _size as size,
  _update as update,
};
