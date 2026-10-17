import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

async function _delete(id: number, init?: EndpointRequestInit): Promise<void> {
  return client.call('ReservedNameEndpoint', '_delete', { id }, init);
}

async function __delete(id: number, init?: EndpointRequestInit): Promise<void> {
  return client.call('ReservedNameEndpoint', 'delete', { id }, init);
}

async function _eval(_arguments: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('ReservedNameEndpoint', 'eval', { arguments: _arguments }, init);
}

async function remove(_delete: number, init?: EndpointRequestInit): Promise<void> {
  return client.call('ReservedNameEndpoint', 'remove', { delete: _delete }, init);
}

async function size(init?: EndpointRequestInit): Promise<number> {
  return client.call('ReservedNameEndpoint', 'size', {}, init);
}

export { _delete, __delete as delete, _eval as eval, remove, size };
