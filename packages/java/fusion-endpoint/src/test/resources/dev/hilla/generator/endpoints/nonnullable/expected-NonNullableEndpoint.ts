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
  init?: EndpointRequestInit
): Promise<Record<string, NonNullableModel | undefined>> {
  return client.call('NonNullableEndpoint', 'echoMap', {shouldBeNotNull}, init);
}

function _echoNonNullModel (
  nonNullableModels: Array<NonNullableModel | undefined>,
  init?: EndpointRequestInit
): Promise<NonNullableModel | undefined> {
  return client.call('NonNullableEndpoint', 'echoNonNullModel', {nonNullableModels}, init);
}

function _echoNonNullableMap(
  nonNullableList: Array<string>,
  init?: EndpointRequestInit
): Promise<Record<string, NonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoNonNullableMap', {nonNullableList}, init);
}

function _echoVaadinNonNullableMap(
  nonNullableParameter: Array<string>,
  init?: EndpointRequestInit
): Promise<Record<string, VaadinNonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoVaadinNonNullableMap', {nonNullableParameter}, init);
}

function _getNonNullableIndex(init?: EndpointRequestInit): Promise<number> {
  return client.call('NonNullableEndpoint', 'getNonNullableIndex', {}, init);
}

function _getNonNullableString(
  input: string | undefined,
  init?: EndpointRequestInit
): Promise<string> {
  return client.call('NonNullableEndpoint', 'getNonNullableString', {input}, init);
}

function _getNotNullReturnType(init?: EndpointRequestInit): Promise<ReturnType | undefined> {
  return client.call('NonNullableEndpoint', 'getNotNullReturnType', {}, init);
}

function _sendParameterType(
  parameterType: ParameterType | undefined,
  init?: EndpointRequestInit
): Promise<void> {
  return client.call('NonNullableEndpoint', 'sendParameterType', {parameterType}, init);
}

function _stringNullable(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NonNullableEndpoint', 'stringNullable', {}, init);
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
