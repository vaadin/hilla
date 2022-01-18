/**
 * This module is generated from NonNullableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module NonNullableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type NonNullableModel from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/NonNullableModel';
import type ParameterType from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/ParameterType';
import type ReturnType from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/ReturnType';
import type VaadinNonNullableModel from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/VaadinNonNullableModel';

function _echoMap(
  shouldBeNotNull: boolean
): Promise<Record<string, NonNullableModel | undefined>> {
  return client.call('NonNullableEndpoint', 'echoMap', {shouldBeNotNull});
}

function _echoNonNullModel(
  nonNullableModels: Array<NonNullableModel | undefined>
): Promise<NonNullableModel | undefined> {
  return client.call('NonNullableEndpoint', 'echoNonNullModel', {nonNullableModels});
}

function _echoNonNullableMap(
  nonNullableList: Array<string>
): Promise<Record<string, NonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoNonNullableMap', {nonNullableList});
}

function _echoVaadinNonNullableMap(
  nonNullableParameter: Array<string>
): Promise<Record<string, VaadinNonNullableModel>> {
  return client.call ('NonNullableEndpoint', 'echoVaadinNonNullableMap', {nonNullableParameter});
}

function _getNonNullableIndex(): Promise<number> {
  return client.call('NonNullableEndpoint', 'getNonNullableIndex');
}

function _getNonNullableString(
  input: string | undefined
): Promise<string> {
  return client.call('NonNullableEndpoint', 'getNonNullableString', {input});
}

function _getNotNullReturnType(): Promise<ReturnType | undefined> {
  return client.call('NonNullableEndpoint', 'getNotNullReturnType');
}

function _sendParameterType(
  parameterType: ParameterType | undefined
): Promise<void> {
  return client.call('NonNullableEndpoint', 'sendParameterType', {parameterType});
}

function _stringNullable(): Promise<string | undefined> {
  return client.call('NonNullableEndpoint', 'stringNullable');
}

export {
  _echoMap as echoMap,
  _echoNonNullModel as echoNonNullModel,
  _echoNonNullableMap as echoNonNullableMap,
  _echoVaadinNonNullableMap as echoVaadinNonNullableMap,
  _getNonNullableIndex as getNonNullableIndex,
  _getNonNullableString as getNonNullableString,
  _getNotNullReturnType as getNotNullReturnType,
  _sendParameterType as sendParameterType,
  _stringNullable as stringNullable,
};
