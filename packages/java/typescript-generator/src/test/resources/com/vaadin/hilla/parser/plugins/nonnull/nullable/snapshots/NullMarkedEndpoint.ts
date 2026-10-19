import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type NullMarkedModel from './com/vaadin/hilla/parser/plugins/nonnull/nullable/nullMarked/NullMarkedEndpoint/NullMarkedModel.js';
import type UnmarkedModel from './com/vaadin/hilla/parser/plugins/nonnull/nullable/nullMarked/unmarked/UnmarkedModel.js';
import client from './connect-client.default.js';

async function nullMarkedModel(nullMarkedModel: NullMarkedModel, init?: EndpointRequestInit): Promise<NullMarkedModel> {
  return client.call('NullMarkedEndpoint', 'nullMarkedModel', { nullMarkedModel }, init);
}

async function nullableMethod(param: string | undefined, init?: EndpointRequestInit): Promise<NullMarkedModel | undefined> {
  return client.call('NullMarkedEndpoint', 'nullableMethod', { param }, init);
}

async function nullableTypeArgumentMethod(param: Array<string | undefined>, init?: EndpointRequestInit): Promise<Array<string | undefined>> {
  return client.call('NullMarkedEndpoint', 'nullableTypeArgumentMethod', { param }, init);
}

async function unmarkedModel(unmarkedModel: UnmarkedModel, init?: EndpointRequestInit): Promise<UnmarkedModel> {
  return client.call('NullMarkedEndpoint', 'unmarkedModel', { unmarkedModel }, init);
}

export { nullMarkedModel, nullableMethod, nullableTypeArgumentMethod, unmarkedModel };
