import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type TransientModel from './com/vaadin/hilla/parser/plugins/backbone/transients/TransientEndpoint/TransientModel.js';
import client from './connect-client.default.js';

export async function getTransientModel(init?: EndpointRequestInit): Promise<TransientModel | undefined> {
  return client.call('TransientEndpoint', 'getTransientModel', {}, init);
}
