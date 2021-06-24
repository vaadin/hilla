/**
 * This class is used for OpenApi generator test
 *
 * This module is generated from GeneratorTestClass.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module GeneratorTestClass
 */

// @ts-ignore
import client from './connect-client.default';
import ComplexRequest from './ComplexRequest';
import ComplexResponse from './ComplexResponse';

function _complexEntitiesTest(
    request?: ComplexRequest
): Promise<ComplexResponse> {
  return client.call('GeneratorTestClass', 'complexEntitiesTest', {request});
}
export {_complexEntitiesTest as complexEntitiesTest};

export const GeneratorTestClass = Object.freeze({
  complexEntitiesTest: _complexEntitiesTest,
});