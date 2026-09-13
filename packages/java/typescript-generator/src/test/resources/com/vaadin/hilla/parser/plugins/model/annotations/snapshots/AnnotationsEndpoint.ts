import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type AnnotationTestEntity from './com/vaadin/hilla/parser/plugins/model/annotations/AnnotationsEndpoint/AnnotationTestEntity.js';
import client from './connect-client.default.js';

export async function getTestEntity(init?: EndpointRequestInit): Promise<AnnotationTestEntity | undefined> {
  return client.call('AnnotationsEndpoint', 'getTestEntity', {}, init);
}
