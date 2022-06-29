/**
 * This module is generated from NonNullableEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module NonNullableEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type NonNullableModel from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/NonNullableModel';
import type ParameterType from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/ParameterType';
import type ReturnType from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/ReturnType';
import type VaadinNonNullableModel from './dev/hilla/generator/endpoints/nonnullable/NonNullableEndpoint/VaadinNonNullableModel';

function _echoMap(
  shouldBeNotNull: boolean,
  endpointRequestInit?: EndpointRequestInit
): Promise<Record<string, NonNullableModel | undefined>> {
  return client.call('NonNullableEndpoint', 'echoMap', {shouldBeNotNull}, endpointRequestInit);
}

function _echoNonNullModel (
  nonNullableModels: Array<NonNullableModel | undefined>,
  endpointRequestInit?: EndpointRequestInit
): Promise<NonNullableModel | undefined> {
  return client.call('NonNullableEndpoint', 'echoNonNullModel', {nonNullableModels}, endpointRequestInit);
}

function _echoNonNullableMap(
  nonNullableList: Array<string>,
  endpointRequestInit?: EndpointRequestInit
): Promise<Record<string, NonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoNonNullableMap', {nonNullableList}, endpointRequestInit);
}

function _echoVaadinNonNullableMap(
  nonNullableParameter: Array<string>,
  endpointRequestInit?: EndpointRequestInit
): Promise<Record<string, VaadinNonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoVaadinNonNullableMap', {nonNullableParameter}, endpointRequestInit);
}

function _getNonNullableIndex(endpointRequestInit?: EndpointRequestInit): Promise<number> {
  return client.call('NonNullableEndpoint', 'getNonNullableIndex', {}, endpointRequestInit);
}

function _getNonNullableString(
  input: string | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<string> {
  return client.call('NonNullableEndpoint', 'getNonNullableString', {input}, endpointRequestInit);
}

function _getNotNullReturnType(endpointRequestInit?: EndpointRequestInit): Promise<ReturnType | undefined> {
  return client.call('NonNullableEndpoint', 'getNotNullReturnType', {}, endpointRequestInit);
}

function _sendParameterType(
  parameterType: ParameterType | undefined,
  endpointRequestInit?: EndpointRequestInit
): Promise<void> {
  return client.call('NonNullableEndpoint', 'sendParameterType', {parameterType}, endpointRequestInit);
}

function _stringNullable(endpointRequestInit?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NonNullableEndpoint', 'stringNullable', {}, endpointRequestInit);
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
