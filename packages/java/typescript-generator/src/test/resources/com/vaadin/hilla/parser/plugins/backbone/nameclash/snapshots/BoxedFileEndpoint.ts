import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Box from './com/vaadin/hilla/parser/plugins/backbone/nameclash/Box.js';
import type File_1 from './com/vaadin/hilla/parser/plugins/backbone/nameclash/File.js';
import client from './connect-client.default.js';

async function upload(file: Box<File | undefined> | undefined, init?: EndpointRequestInit): Promise<File_1 | undefined> {
  return client.call('BoxedFileEndpoint', 'upload', { file }, init);
}

export { upload };
