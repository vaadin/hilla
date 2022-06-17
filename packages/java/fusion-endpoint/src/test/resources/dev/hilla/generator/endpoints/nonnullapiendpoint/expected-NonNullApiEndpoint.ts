/**
* This module is generated from NonNullApiEndpoint.java
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
* @module NonNullApiEndpoint
*/
// @ts-ignore
import client from './connect-client.default';
// @ts-ignore
import {
    EndpointRequestInit, Subscription
}
from '@hilla/frontend';
function _helloFlux (
  hello: string
):
Subscription<string> {
  return client.subscribe (
    'NonNullApiEndpoint', 'helloFlux', {
      hello
    }
  );
}
function _helloNestedTypes (
  param: Record<string, Array<string>>,
  init?: EndpointRequestInit
): Promise<Record<string, Array<Record<string, string>>>> {
  return client.call (
    'NonNullApiEndpoint', 'helloNestedTypes', {
      param
    }, init
  );
}
function _helloNullable (
  hello: string | undefined,
  init?: EndpointRequestInit
): Promise<string | undefined> {
  return client.call (
    'NonNullApiEndpoint', 'helloNullable', {
      hello
    }, init
  );
}
function _hello (
  hello: string,
  init?: EndpointRequestInit
): Promise<string> {
  return client.call (
    'NonNullApiEndpoint', 'hello', {
      hello
    }, init
  );
}
export {
  _helloFlux as helloFlux,
  _helloNestedTypes as helloNestedTypes,
  _helloNullable as helloNullable,
  _hello as hello,
};
