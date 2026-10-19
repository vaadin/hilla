import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type NullableFieldModel from './com/vaadin/hilla/parser/plugins/nonnull/nullable/NullableEndpoint/NullableFieldModel.js';
import client from './connect-client.default.js';

async function nullMarkedMethod(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NullableEndpoint', 'nullMarkedMethod', {}, init);
}

async function nullableFieldModel(nullableFieldModel: NullableFieldModel | undefined, init?: EndpointRequestInit): Promise<NullableFieldModel | undefined> {
  return client.call('NullableEndpoint', 'nullableFieldModel', { nullableFieldModel }, init);
}

export { nullMarkedMethod, nullableFieldModel };
