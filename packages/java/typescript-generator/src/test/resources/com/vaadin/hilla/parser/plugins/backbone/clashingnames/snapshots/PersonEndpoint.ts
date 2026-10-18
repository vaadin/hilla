import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Person from './com/vaadin/hilla/parser/plugins/backbone/clashingnames/one/Person.js';
import type Person_2 from './com/vaadin/hilla/parser/plugins/backbone/clashingnames/three/Person.js';
import type Person_1 from './com/vaadin/hilla/parser/plugins/backbone/clashingnames/two/Person.js';
import client from './connect-client.default.js';

async function first(init?: EndpointRequestInit): Promise<Person | undefined> {
  return client.call('PersonEndpoint', 'first', {}, init);
}

async function second(init?: EndpointRequestInit): Promise<Person_1 | undefined> {
  return client.call('PersonEndpoint', 'second', {}, init);
}

async function third(init?: EndpointRequestInit): Promise<Person_2 | undefined> {
  return client.call('PersonEndpoint', 'third', {}, init);
}

export { first, second, third };
