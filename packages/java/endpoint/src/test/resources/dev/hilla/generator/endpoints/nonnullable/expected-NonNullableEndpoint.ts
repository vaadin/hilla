/**
 * This module is generated from NonNullableEndpoint.java
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
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
  __init?: EndpointRequestInit
): Promise<Record<string, NonNullableModel | undefined>> {
  return client.call('NonNullableEndpoint', 'echoMap', {shouldBeNotNull}, __init);
}

function _echoNonNullModel (
  nonNullableModels: Array<NonNullableModel | undefined>,
  __init?: EndpointRequestInit
): Promise<NonNullableModel | undefined> {
  return client.call('NonNullableEndpoint', 'echoNonNullModel', {nonNullableModels}, __init);
}

function _echoNonNullableMap(
  nonNullableList: Array<string>,
  __init?: EndpointRequestInit
): Promise<Record<string, NonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoNonNullableMap', {nonNullableList}, __init);
}

function _echoVaadinNonNullableMap(
  nonNullableParameter: Array<string>,
  __init?: EndpointRequestInit
): Promise<Record<string, VaadinNonNullableModel>> {
  return client.call('NonNullableEndpoint', 'echoVaadinNonNullableMap', {nonNullableParameter}, __init);
}

function _getNonNullableIndex(__init?: EndpointRequestInit): Promise<number> {
  return client.call('NonNullableEndpoint', 'getNonNullableIndex', {}, __init);
}

function _getNonNullableString(
  input: string | undefined,
  __init?: EndpointRequestInit
): Promise<string> {
  return client.call('NonNullableEndpoint', 'getNonNullableString', {input}, __init);
}

function _getNotNullReturnType(__init?: EndpointRequestInit): Promise<ReturnType | undefined> {
  return client.call('NonNullableEndpoint', 'getNotNullReturnType', {}, __init);
}

function _returnNonnullMappedType(__init?: EndpointRequestInit): Promise<Array<string>> {
  return client.call('NonNullableEndpoint', 'returnNonnullMappedType', {}, __init);
}

function _sendParameterType(
  parameterType: ParameterType | undefined,
  __init?: EndpointRequestInit
): Promise<void> {
  return client.call('NonNullableEndpoint', 'sendParameterType', {parameterType}, __init);
}

function _stringNullable(__init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NonNullableEndpoint', 'stringNullable', {}, __init);
}

export {
  _echoMap as echoMap,
  _echoNonNullModel as echoNonNullModel,
  _echoNonNullableMap as echoNonNullableMap,
  _echoVaadinNonNullableMap as echoVaadinNonNullableMap,
  _getNonNullableIndex as getNonNullableIndex,
  _getNonNullableString as getNonNullableString,
  _getNotNullReturnType as getNotNullReturnType,
  _returnNonnullMappedType as returnNonnullMappedType,
  _sendParameterType as sendParameterType,
  _stringNullable as stringNullable,
};
