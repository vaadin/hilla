/**
 * This module is generated from SameClassNameEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SameClassNameEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type SameClassNameModel from './com/vaadin/fusion/generator/endpoints/sameclassname/SameClassNameEndpoint/SameClassNameModel';
import type SubpackageSameClassNameModel from './com/vaadin/fusion/generator/endpoints/sameclassname/subpackage/SameClassNameModel';

function _getMyClass(
  sameClassNameModel: Array<SubpackageSameClassNameModel | undefined> | undefined
): Promise<SameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getMyClass', {sameClassNameModel});
}

function _getSubpackageModelList(
  sameClassNameModel: Record<string, SubpackageSameClassNameModel | undefined> | undefined
): Promise<Array<SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelList', {sameClassNameModel});
}

function _getSubpackageModelMap(
  sameClassNameModel: Record<string, SameClassNameModel | undefined> | undefined
): Promise<Record<string, SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelMap', {sameClassNameModel});
}

function _getSubpackageModel(): Promise<SubpackageSameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModel');
}

function _setSubpackageModel(
  model: SubpackageSameClassNameModel | undefined
): Promise<void> {
  return client.call('SameClassNameEndpoint', 'setSubpackageModel', {model});
}

export {
  _getMyClass as getMyClass,
  _getSubpackageModelList as getSubpackageModelList,
  _getSubpackageModelMap as getSubpackageModelMap,
  _getSubpackageModel as getSubpackageModel,
  _setSubpackageModel as setSubpackageModel,
};
