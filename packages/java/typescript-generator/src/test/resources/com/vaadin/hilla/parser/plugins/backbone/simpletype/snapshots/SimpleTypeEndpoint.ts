import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function doSomething(init?: EndpointRequestInit): Promise<void> {
  return client.call('SimpleTypeEndpoint', 'doSomething', {}, init);
}

export async function getArray(init?: EndpointRequestInit): Promise<Array<number> | undefined> {
  return client.call('SimpleTypeEndpoint', 'getArray', {}, init);
}

export async function getBigDecimal(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getBigDecimal', {}, init);
}

export async function getBigInteger(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getBigInteger', {}, init);
}

export async function getBoolean(init?: EndpointRequestInit): Promise<boolean> {
  return client.call('SimpleTypeEndpoint', 'getBoolean', {}, init);
}

export async function getBooleanWrapper(init?: EndpointRequestInit): Promise<boolean | undefined> {
  return client.call('SimpleTypeEndpoint', 'getBooleanWrapper', {}, init);
}

export async function getByte(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getByte', {}, init);
}

export async function getByteWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getByteWrapper', {}, init);
}

export async function getChar(init?: EndpointRequestInit): Promise<string> {
  return client.call('SimpleTypeEndpoint', 'getChar', {}, init);
}

export async function getCharWrapper(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('SimpleTypeEndpoint', 'getCharWrapper', {}, init);
}

export async function getDouble(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getDouble', {}, init);
}

export async function getDoubleWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getDoubleWrapper', {}, init);
}

export async function getFloat(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getFloat', {}, init);
}

export async function getFloatWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getFloatWrapper', {}, init);
}

export async function getInteger(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getInteger', {}, init);
}

export async function getIntegerWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getIntegerWrapper', {}, init);
}

export async function getLong(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getLong', {}, init);
}

export async function getLongWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getLongWrapper', {}, init);
}

export async function getShort(init?: EndpointRequestInit): Promise<number> {
  return client.call('SimpleTypeEndpoint', 'getShort', {}, init);
}

export async function getShortWrapper(init?: EndpointRequestInit): Promise<number | undefined> {
  return client.call('SimpleTypeEndpoint', 'getShortWrapper', {}, init);
}

export async function getString(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('SimpleTypeEndpoint', 'getString', {}, init);
}
