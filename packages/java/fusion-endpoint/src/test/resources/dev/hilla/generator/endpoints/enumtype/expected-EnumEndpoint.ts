/**
 * This module is generated from EnumEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module EnumEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import { EndpointRequestInit, Subscription } from '@hilla/frontend';
import type MyEnum from './dev/hilla/generator/endpoints/enumtype/EnumEndpoint/MyEnum';

function _echoEnum(
    value: MyEnum | undefined,
    init?: EndpointRequestInit
): Promise<MyEnum | undefined> {
  return client.call('EnumEndpoint', 'echoEnum', {value}, init);
}

function _echoListEnum(
    enumList: Array<MyEnum | undefined> | undefined,
    init?: EndpointRequestInit
): Promise<Array<MyEnum | undefined> | undefined> {
  return client.call('EnumEndpoint', 'echoListEnum', {enumList}, init);
}

function _getEnum(init?: EndpointRequestInit): Promise<MyEnum | undefined> {
  return client.call('EnumEndpoint', 'getEnum', {}, init);
}

function _setEnum(
    value: MyEnum | undefined,
    init?: EndpointRequestInit
): Promise<void> {
  return client.call('EnumEndpoint', 'setEnum', {value}, init);
}

export {
  _echoEnum as echoEnum,
  _echoListEnum as echoListEnum,
  _getEnum as getEnum,
  _setEnum as setEnum,
};
