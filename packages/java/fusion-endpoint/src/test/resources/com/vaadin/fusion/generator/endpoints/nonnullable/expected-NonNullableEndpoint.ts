/**
 * This module is generated from NonNullableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module NonNullableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import NonNullableModel from './com/vaadin/fusion/generator/endpoints/nonnullable/NonNullableEndpoint/NonNullableModel';
import ParameterType from './com/vaadin/fusion/generator/endpoints/nonnullable/NonNullableEndpoint/ParameterType';
import ReturnType from './com/vaadin/fusion/generator/endpoints/nonnullable/NonNullableEndpoint/ReturnType';

function _echoMap(
  shouldBeNotNull: boolean
): Promise<Record<string, NonNullableModel | undefined>> {
  return client.call('NonNullableEndpoint', 'echoMap', {shouldBeNotNull});
}

export {_echoMap as echoMap};

function _echoNonNullModel(
  nonNullableModels: Array<NonNullableModel | undefined>
): Promise<NonNullableModel | undefined> {
  return client.call('NonNullableEndpoint', 'echoNonNullModel', {nonNullableModels});
}

export {_echoNonNullModel as echoNonNullModel};

function _echoNonNullableMap(
  nonNullableList: Array<string>
): Promise<Record<string, NonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoNonNullableMap', {nonNullableList});
}

export {_echoNonNullableMap as echoNonNullableMap};

function _getNotNullReturnType(): Promise<ReturnType | undefined> {
  return client.call('NonNullableEndpoint', 'getNotNullReturnType');
}

export {_getNotNullReturnType as getNotNullReturnType};

function _getNullableString(
  input: string | undefined
): Promise<string> {
  return client.call('NonNullableEndpoint', 'getNullableString', {input});
}

export {_getNullableString as getNullableString};

function _sendParameterType(
  parameterType: ParameterType | undefined
): Promise<void> {
  return client.call('NonNullableEndpoint', 'sendParameterType', {parameterType});
}

export {_sendParameterType as sendParameterType};

function _stringNullable(): Promise<string | undefined> {
  return client.call('NonNullableEndpoint', 'stringNullable');
}

export {_stringNullable as stringNullable};

export const NonNullableEndpoint = Object.freeze({
  echoMap: _echoMap,
  echoNonNullModel: _echoNonNullModel,
  echoNonNullableMap: _echoNonNullableMap,
  getNotNullReturnType: _getNotNullReturnType,
  getNullableString: _getNullableString,
  sendParameterType: _sendParameterType,
  stringNullable: _stringNullable,
});