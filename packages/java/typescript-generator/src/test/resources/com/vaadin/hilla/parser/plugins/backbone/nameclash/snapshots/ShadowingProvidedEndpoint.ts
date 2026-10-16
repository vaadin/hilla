import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type File_1 from './com/vaadin/hilla/parser/plugins/backbone/nameclash/ShadowingProvidedEndpoint/File.js';
import client from './connect-client.default.js';

export async function describe(file: File | undefined, init?: EndpointRequestInit): Promise<File_1 | undefined> {
  return client.call('ShadowingProvidedEndpoint', 'describe', { file }, init);
}
