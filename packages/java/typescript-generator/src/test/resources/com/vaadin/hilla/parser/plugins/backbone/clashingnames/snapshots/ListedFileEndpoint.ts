import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type File_1 from './com/vaadin/hilla/parser/plugins/backbone/clashingnames/File.js';
import client from './connect-client.default.js';

async function upload(files: Array<File | undefined> | undefined, init?: EndpointRequestInit): Promise<File_1 | undefined> {
  return client.call('ListedFileEndpoint', 'upload', { files }, init);
}

export { upload };
