/**
 * This module is generated from EnumEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module EnumEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import MyEnum from './com/vaadin/flow/server/connect/generator/endpoints/enumtype/EnumEndpoint/MyEnum';

function _echoEnum(
    value: MyEnum
): Promise<MyEnum> {
  return client.call('EnumEndpoint', 'echoEnum', {value});
}
export {_echoEnum as echoEnum};

function _echoListEnum(
    enumList: Array<MyEnum>
): Promise<Array<MyEnum>> {
  return client.call('EnumEndpoint', 'echoListEnum', {enumList});
}
export {_echoListEnum as echoListEnum};

function _getEnum(): Promise<MyEnum> {
  return client.call('EnumEndpoint', 'getEnum');
}
export {_getEnum as getEnum};

function _setEnum(
    value: MyEnum
): Promise<void> {
  return client.call('EnumEndpoint', 'setEnum', {value});
}
export {_setEnum as setEnum};