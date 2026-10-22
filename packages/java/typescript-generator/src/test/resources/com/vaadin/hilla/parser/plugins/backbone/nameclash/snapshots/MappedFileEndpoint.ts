import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type File_1 from './com/vaadin/hilla/parser/plugins/backbone/nameclash/File.js';
import client from './connect-client.default.js';

async function upload(files: Record<string, File | undefined> | undefined, init?: EndpointRequestInit): Promise<File_1 | undefined> {
  return client.call('MappedFileEndpoint', 'upload', { files }, init);
}

export { upload };
