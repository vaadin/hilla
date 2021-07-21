/**
 * This module is generated from ComplexTypeParamsEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module ComplexTypeParamsEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type Account from './com/vaadin/fusion/generator/endpoints/model/ModelEndpoint/Account';
import type Group from './com/vaadin/fusion/generator/endpoints/model/ModelEndpoint/Group';

function _getComplexTypeParams(
  accounts: ReadonlyArray<Account | undefined> | undefined,
  groups: Readonly<Record<string, Group | undefined>> | undefined
): Promise<void> {
  return client.call('ComplexTypeParamsEndpoint', 'getComplexTypeParams', {accounts, groups});
}

function _send(
  text: string | undefined,
  chatPartnerId: number
 ): Promise<void> {
  return client.call('ComplexTypeParamsEndpoint', 'send', {text, chatPartnerId});
 }

export {
  _getComplexTypeParams as getComplexTypeParams,
  _send as send,
};
