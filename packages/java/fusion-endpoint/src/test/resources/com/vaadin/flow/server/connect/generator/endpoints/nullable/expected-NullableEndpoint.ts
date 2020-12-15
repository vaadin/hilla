/**
 * This module is generated from NullableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module NullableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import NullableModel from './com/vaadin/flow/server/connect/generator/endpoints/nullable/NullableEndpoint/NullableModel';
import ParameterType from './com/vaadin/flow/server/connect/generator/endpoints/nullable/NullableEndpoint/ParameterType';
import ReturnType from './com/vaadin/flow/server/connect/generator/endpoints/nullable/NullableEndpoint/ReturnType';

function _echoMap(
  shouldBeNotNull: boolean
): Promise<{ [key: string]: NullableModel; }> {
  return client.call('NullableEndpoint', 'echoMap', {shouldBeNotNull});
}
export {_echoMap as echoMap};

function _echoNonNullMode(
  nullableModels: Array<NullableModel>
): Promise<NullableModel | undefined> {
  return client.call('NullableEndpoint', 'echoNonNullMode', {nullableModels});
}
export {_echoNonNullMode as echoNonNullMode};

function _getNotNullReturnType(): Promise<ReturnType | undefined> {
  return client.call('NullableEndpoint', 'getNotNullReturnType');
}
export {_getNotNullReturnType as getNotNullReturnType};

function _getNullableString(
  input?: string
): Promise<string> {
  return client.call('NullableEndpoint', 'getNullableString', {input});
}
export {_getNullableString as getNullableString};

function _sendParameterType(
  parameterType?: ParameterType
): Promise<void> {
  return client.call('NullableEndpoint', 'sendParameterType', {parameterType});
}
export {_sendParameterType as sendParameterType};

function _stringNullable(): Promise<string | undefined> {
  return client.call('NullableEndpoint', 'stringNullable');
}
export {_stringNullable as stringNullable};