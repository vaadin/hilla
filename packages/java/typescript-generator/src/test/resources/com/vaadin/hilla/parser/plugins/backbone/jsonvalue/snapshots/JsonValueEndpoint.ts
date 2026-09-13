import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Person from './com/vaadin/hilla/parser/plugins/backbone/jsonvalue/JsonValueEndpoint/Person.js';
import client from './connect-client.default.js';

export async function getEmail(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('JsonValueEndpoint', 'getEmail', {}, init);
}

export async function getPerson(init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('JsonValueEndpoint', 'getPerson', {}, init);
}

export async function setEmail(email: string | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('JsonValueEndpoint', 'setEmail', { email }, init);
}

export async function setPerson(person: Person | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('JsonValueEndpoint', 'setPerson', { person }, init);
}
