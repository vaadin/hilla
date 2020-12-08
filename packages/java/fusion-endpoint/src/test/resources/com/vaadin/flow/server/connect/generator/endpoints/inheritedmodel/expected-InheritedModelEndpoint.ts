/**
 * This module is generated from InheritedModelEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module InheritedModelEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import ChildModel from './com/vaadin/flow/server/connect/generator/endpoints/inheritedmodel/InheritedModelEndpoint/ChildModel';
import ParentModel from './com/vaadin/flow/server/connect/generator/endpoints/inheritedmodel/InheritedModelEndpoint/ParentModel';

function _getParentModel(
  child: ChildModel
): Promise<ParentModel> {
  return client.call('InheritedModelEndpoint', 'getParentModel', {child});
}
export {_getParentModel as getParentModel};