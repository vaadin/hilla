/**
 * This module is generated from SameClassNameEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SameClassNameEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type SameClassNameModel from './dev/hilla/generator/endpoints/sameclassname/SameClassNameEndpoint/SameClassNameModel';
import type SubpackageSameClassNameModel from './dev/hilla/generator/endpoints/sameclassname/subpackage/SameClassNameModel';

function _getMyClass(
  sameClassNameModel: Array<SubpackageSameClassNameModel | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<SameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getMyClass', {sameClassNameModel}, __init);
}

function _getSubpackageModelList(
  sameClassNameModel: Record<string, SubpackageSameClassNameModel | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<Array<SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelList', {sameClassNameModel}, __init);
}

function _getSubpackageModelMap(
  sameClassNameModel: Record<string, SameClassNameModel | undefined> | undefined,
  __init?: EndpointRequestInit
): Promise<Record<string, SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelMap', {sameClassNameModel}, __init);
}

function _getSubpackageModel(__init?: EndpointRequestInit): Promise<SubpackageSameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModel', {}, __init);
}

function _setSubpackageModel(
  model: SubpackageSameClassNameModel | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('SameClassNameEndpoint', 'setSubpackageModel', {model}, __init);
}

export {
  _getMyClass as getMyClass,
  _getSubpackageModelList as getSubpackageModelList,
  _getSubpackageModelMap as getSubpackageModelMap,
  _getSubpackageModel as getSubpackageModel,
  _setSubpackageModel as setSubpackageModel,
};
