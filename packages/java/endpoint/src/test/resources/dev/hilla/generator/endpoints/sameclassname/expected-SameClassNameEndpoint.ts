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
  endpointRequestInit?: EndpointRequestInit
): Promise<SameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getMyClass', {sameClassNameModel}, endpointRequestInit);
}

function _getSubpackageModelList(
  sameClassNameModel: Record<string, SubpackageSameClassNameModel | undefined> | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<Array<SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelList', {sameClassNameModel}, endpointRequestInit);
}

function _getSubpackageModelMap(
  sameClassNameModel: Record<string, SameClassNameModel | undefined> | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<Record<string, SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelMap', {sameClassNameModel}, endpointRequestInit);
}

function _getSubpackageModel(endpointRequestInit?: EndpointRequestInit): Promise<SubpackageSameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModel', {}, endpointRequestInit);
}

function _setSubpackageModel(
  model: SubpackageSameClassNameModel | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('SameClassNameEndpoint', 'setSubpackageModel', {model}, endpointRequestInit);
}

export {
  _getMyClass as getMyClass,
  _getSubpackageModelList as getSubpackageModelList,
  _getSubpackageModelMap as getSubpackageModelMap,
  _getSubpackageModel as getSubpackageModel,
  _setSubpackageModel as setSubpackageModel,
};
