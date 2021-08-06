/**
 * This module is generated from ComplexTypeEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexTypeEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type ComplexTypeModel from './com/vaadin/fusion/generator/endpoints/complextypes/ComplexTypeEndpoint/ComplexTypeModel';

function _getComplexTypeModel(
  data: ReadonlyArray<Readonly<Record<string, string | undefined>> | undefined> | undefined
): Promise<ComplexTypeModel | undefined> {
  return client.call('ComplexTypeEndpoint', 'getComplexTypeModel', {data});
}

export {
  _getComplexTypeModel as getComplexTypeModel,
};
