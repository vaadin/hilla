import type { EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';
import type Bounded from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Bounded.js';
import type Detailed from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Detailed.js';
import type Figure from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Figure.js';
import type Kind from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Kind.js';
import type Marker from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Marker.js';
import type Mixed from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Mixed.js';
import type Sample from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Sample.js';
import type Shaded from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Shaded.js';
import type Wrapper from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Wrapper.js';
import client from './connect-client.default.js';

async function all(init?: EndpointRequestInit): Promise<Array<Sample | undefined> | undefined> {
  return client.call('SampleEndpoint', 'all', {}, init);
}

async function bounded(init?: EndpointRequestInit): Promise<Bounded | undefined> {
  return client.call('SampleEndpoint', 'bounded', {}, init);
}

async function count(init?: EndpointRequestInit): Promise<number> {
  return client.call('SampleEndpoint', 'count', {}, init);
}

async function counts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
  return client.call('SampleEndpoint', 'counts', {}, init);
}

async function describe(firstName: string | undefined, lastName: string | undefined, age: number, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('SampleEndpoint', 'describe', { firstName, lastName, age }, init);
}

async function detailed(init?: EndpointRequestInit): Promise<Detailed | undefined> {
  return client.call('SampleEndpoint', 'detailed', {}, init);
}

async function figure(init?: EndpointRequestInit): Promise<Figure | undefined> {
  return client.call('SampleEndpoint', 'figure', {}, init);
}

async function find(id: string | undefined, init?: EndpointRequestInit): Promise<Sample | undefined> {
  return client.call('SampleEndpoint', 'find', { id }, init);
}

async function greet(name: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('SampleEndpoint', 'greet', { name }, init);
}

async function kind(init?: EndpointRequestInit): Promise<Kind | undefined> {
  return client.call('SampleEndpoint', 'kind', {}, init);
}

async function marker(init?: EndpointRequestInit): Promise<Marker | undefined> {
  return client.call('SampleEndpoint', 'marker', {}, init);
}

async function maybe(init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('SampleEndpoint', 'maybe', {}, init);
}

async function maybeCounts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
  return client.call('SampleEndpoint', 'maybeCounts', {}, init);
}

async function maybeNames(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
  return client.call('SampleEndpoint', 'maybeNames', {}, init);
}

async function mixed(init?: EndpointRequestInit): Promise<Mixed | undefined> {
  return client.call('SampleEndpoint', 'mixed', {}, init);
}

async function names(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
  return client.call('SampleEndpoint', 'names', {}, init);
}

async function ping(init?: EndpointRequestInit): Promise<void> {
  return client.call('SampleEndpoint', 'ping', {}, init);
}

async function required(name: string, init?: EndpointRequestInit): Promise<string> {
  return client.call('SampleEndpoint', 'required', { name }, init);
}

async function shaded(init?: EndpointRequestInit): Promise<Shaded | undefined> {
  return client.call('SampleEndpoint', 'shaded', {}, init);
}

function stream(count: number): Subscription<string | undefined> {
  return client.subscribe('SampleEndpoint', 'stream', { count });
}

function watch(): Subscription<Sample | undefined> {
  return client.subscribe('SampleEndpoint', 'watch', {});
}

async function wrapped(init?: EndpointRequestInit): Promise<Wrapper<Sample | undefined> | undefined> {
  return client.call('SampleEndpoint', 'wrapped', {}, init);
}

export { all, bounded, count, counts, describe, detailed, figure, find, greet, kind, marker, maybe, maybeCounts, maybeNames, mixed, names, ping, required, shaded, stream, watch, wrapped };
