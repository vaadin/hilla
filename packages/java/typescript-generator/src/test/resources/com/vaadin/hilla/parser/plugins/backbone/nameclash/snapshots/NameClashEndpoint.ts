import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

async function getOneParam(init: string | undefined, _init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getOneParam', { init }, _init);
}

async function getThreeParams(__init: string | undefined, _init: string | undefined, init: string | undefined, ___init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getThreeParams', { __init, _init, init }, ___init);
}

async function getTwoParams(init: string | undefined, _init: string | undefined, __init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NameClashEndpoint', 'getTwoParams', { init, _init }, __init);
}

export { getOneParam, getThreeParams, getTwoParams };
