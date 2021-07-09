/**
 * This module is generated from ComplexTypeParamsEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexTypeParamsEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/fusion/generator/endpoints/model/ModelEndpoint/Account';
import Group from './com/vaadin/fusion/generator/endpoints/model/ModelEndpoint/Group';

function _getComplexTypeParams(
    accounts: ReadonlyArray<Account | undefined> | undefined,
    groups: Readonly<Record<string, Group | undefined>> | undefined
): Promise<void> {
  return client.call('ComplexTypeParamsEndpoint', 'getComplexTypeParams', {accounts, groups});
}
export {_getComplexTypeParams as getComplexTypeParams};

function _send(
  text: string | undefined,
  chatPartnerId: number
 ): Promise<void> {
  return client.call('ComplexTypeParamsEndpoint', 'send', {text, chatPartnerId});
 }
export {_send as send};

export const ComplexTypeParamsEndpoint = Object.freeze({
  getComplexTypeParams: _getComplexTypeParams,
  send: _send,
});