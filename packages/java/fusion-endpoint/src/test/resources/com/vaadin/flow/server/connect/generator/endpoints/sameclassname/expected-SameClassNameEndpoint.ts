/**
 * This module is generated from SameClassNameEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SameClassNameEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import SameClassNameModel from './com/vaadin/flow/server/connect/generator/endpoints/sameclassname/SameClassNameEndpoint/SameClassNameModel';
import SubpackageSameClassNameModel from './com/vaadin/flow/server/connect/generator/endpoints/sameclassname/subpackage/SameClassNameModel';

function _getMyClass(
    sameClassNameModel?: Array<SubpackageSameClassNameModel | undefined>
): Promise<SameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getMyClass', {sameClassNameModel});
}
export {_getMyClass as getMyClass};

function _getSubpackageModelList(
    sameClassNameModel?: Record<string, SubpackageSameClassNameModel | undefined>
): Promise<Array<SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelList', {sameClassNameModel});
}
export {_getSubpackageModelList as getSubpackageModelList};

function _getSubpackageModelMap(
    sameClassNameModel?: Record<string, SameClassNameModel | undefined>
): Promise<Record<string, SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelMap', {sameClassNameModel});
}
export {_getSubpackageModelMap as getSubpackageModelMap};

function _getSubpackageModel(): Promise<SubpackageSameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModel');
}
export {_getSubpackageModel as getSubpackageModel};

function _setSubpackageModel(
    model?: SubpackageSameClassNameModel
): Promise<void> {
  return client.call('SameClassNameEndpoint', 'setSubpackageModel', {model});
}
export {_setSubpackageModel as setSubpackageModel};

export const SameClassNameEndpoint = Object.freeze({
  getMyClass: _getMyClass,
  getSubpackageModelList: _getSubpackageModelList,
  getSubpackageModelMap: _getSubpackageModelMap,
  getSubpackageModel: _getSubpackageModel,
  setSubpackageModel: _setSubpackageModel,
});