import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export async function uploadFile(file: File | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('MultipartFileEndpoint', 'uploadFile', { file }, init);
}
