/**
 * This module is generated from SameClassNameEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module SameClassNameEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import SameClassNameModel from './com/vaadin/fusion/generator/endpoints/sameclassname/SameClassNameEndpoint/SameClassNameModel';
import SubpackageSameClassNameModel from './com/vaadin/fusion/generator/endpoints/sameclassname/subpackage/SameClassNameModel';

function _getMyClass(
    sameClassNameModel: ReadonlyArray<SubpackageSameClassNameModel | undefined> | undefined
): Promise<SameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getMyClass', {sameClassNameModel});
}
export {_getMyClass as getMyClass};

function _getSubpackageModelList(
    sameClassNameModel: Readonly<Record<string, SubpackageSameClassNameModel | undefined>> | undefined
): Promise<ReadonlyArray<SubpackageSameClassNameModel | undefined> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelList', {sameClassNameModel});
}
export {_getSubpackageModelList as getSubpackageModelList};

function _getSubpackageModelMap(
    sameClassNameModel: Readonly<Record<string, SameClassNameModel | undefined>> | undefined
): Promise<Readonly<Record<string, SubpackageSameClassNameModel | undefined>> | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModelMap', {sameClassNameModel});
}
export {_getSubpackageModelMap as getSubpackageModelMap};

function _getSubpackageModel(): Promise<SubpackageSameClassNameModel | undefined> {
  return client.call('SameClassNameEndpoint', 'getSubpackageModel');
}
export {_getSubpackageModel as getSubpackageModel};

function _setSubpackageModel(
    model: SubpackageSameClassNameModel | undefined
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