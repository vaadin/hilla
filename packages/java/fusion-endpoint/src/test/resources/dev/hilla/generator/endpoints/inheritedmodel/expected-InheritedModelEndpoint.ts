/**
 * This module is generated from InheritedModelEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module InheritedModelEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { Subscription } from '@hilla/frontend';
import type ChildModel from './dev/hilla/generator/endpoints/inheritedmodel/InheritedModelEndpoint/ChildModel';
import type ParentModel from './dev/hilla/generator/endpoints/inheritedmodel/InheritedModelEndpoint/ParentModel';

function _getParentModel(
  child: ChildModel | undefined
): Promise<ParentModel | undefined> {
  return client.call('InheritedModelEndpoint', 'getParentModel', {child});
}

export {
  _getParentModel as getParentModel,
};
