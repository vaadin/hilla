import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Annotated from './com/vaadin/hilla/parser/plugins/model/annotatedtypes/AnnotatedTypesEndpoint/Annotated.js';
import client from './connect-client.default.js';

async function getAnnotated(init?: EndpointRequestInit): Promise<Annotated | undefined> {
  return client.call('AnnotatedTypesEndpoint', 'getAnnotated', {}, init);
}

export { getAnnotated };
