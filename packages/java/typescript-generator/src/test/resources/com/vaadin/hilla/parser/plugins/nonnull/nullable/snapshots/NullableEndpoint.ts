import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type NullableFieldModel from './com/vaadin/hilla/parser/plugins/nonnull/nullable/NullableEndpoint/NullableFieldModel.js';
import type NullMarkedModel from './com/vaadin/hilla/parser/plugins/nonnull/nullable/nullmarked/NullMarkedEndpoint/NullMarkedModel.js';
import client from './connect-client.default.js';

async function nullMarkedMethod(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NullableEndpoint', 'nullMarkedMethod', {}, init);
}

async function nullMarkedModel(nullMarkedModel: NullMarkedModel | undefined, init?: EndpointRequestInit): Promise<NullMarkedModel | undefined> {
  return client.call('NullableEndpoint', 'nullMarkedModel', { nullMarkedModel }, init);
}

async function nullableFieldModel(nullableFieldModel: NullableFieldModel | undefined, init?: EndpointRequestInit): Promise<NullableFieldModel | undefined> {
  return client.call('NullableEndpoint', 'nullableFieldModel', { nullableFieldModel }, init);
}

export { nullMarkedMethod, nullMarkedModel, nullableFieldModel };
