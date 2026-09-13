import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type User from './com/vaadin/hilla/parser/plugins/backbone/jsonvaluenojsoncreator/JsonCreatorNoJsonValueEndpoint/User.js';
import client from './connect-client.default.js';

export async function getUser(init?: EndpointRequestInit): Promise<User | undefined> {
  return client.call('JsonCreatorNoJsonValueEndpoint', 'getUser', {}, init);
}

export async function setUser(user: User | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('JsonCreatorNoJsonValueEndpoint', 'setUser', { user }, init);
}
