/**
 * This module is generated from ComplexTypeParamsEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexTypeParamsEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/flow/server/connect/generator/endpoints/model/ModelEndpoint/Account';
import Group from './com/vaadin/flow/server/connect/generator/endpoints/model/ModelEndpoint/Group';

function _getComplexTypeParams(
    accounts?: Array<Account>,
    groups?: { [key: string]: Group; }
): Promise<void> {
  return client.call('ComplexTypeParamsEndpoint', 'getComplexTypeParams', {accounts, groups});
}
export {_getComplexTypeParams as getComplexTypeParams};

export const ComplexTypeParamsEndpoint = Object.freeze({
  getComplexTypeParams: _getComplexTypeParams,
});