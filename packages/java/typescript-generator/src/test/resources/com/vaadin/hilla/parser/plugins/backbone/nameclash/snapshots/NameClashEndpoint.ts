import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function getOneParam(init: string | undefined, _init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getOneParam', { init }, _init);
}

export async function getThreeParams(
  __init: string | undefined,
  _init: string | undefined,
  init: string | undefined,
  ___init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getThreeParams', { __init, _init, init }, ___init);
}

export async function getTwoParams(
  init: string | undefined,
  _init: string | undefined,
  __init?: EndpointRequestInit,
): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getTwoParams', { init, _init }, __init);
}
