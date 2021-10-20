/**
 * This module is generated from EnumEndpoint.java
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 * @module EnumEndpoint
 */

// @ts-ignore
import client from './connect-client.default';
import type MyEnum from './com/vaadin/fusion/generator/endpoints/enumtype/EnumEndpoint/MyEnum';

function _echoEnum(
    value: MyEnum | undefined
): Promise<MyEnum | undefined> {
  return client.call('EnumEndpoint', 'echoEnum', {value});
}

function _echoListEnum(
    enumList: Array<MyEnum | undefined> | undefined
): Promise<Array<MyEnum | undefined> | undefined> {
  return client.call('EnumEndpoint', 'echoListEnum', {enumList});
}

function _getEnum(): Promise<MyEnum | undefined> {
  return client.call('EnumEndpoint', 'getEnum');
}

function _setEnum(
    value: MyEnum | undefined
): Promise<void> {
  return client.call('EnumEndpoint', 'setEnum', {value});
}

export {
  _echoEnum as echoEnum,
  _echoListEnum as echoListEnum,
  _getEnum as getEnum,
  _setEnum as setEnum,
};
