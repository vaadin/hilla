import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Dependency from './com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/NonNullApiEndpoint/Dependency.js';
import type Generic from './com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/NonNullApiEndpoint/Generic.js';
import type SubPackageDependency from './com/vaadin/hilla/parser/plugins/nonnull/nonnullapi/subpackage/SubPackageDependency.js';
import client from './connect-client.default.js';

export async function defaultMethod(param: string, init?: EndpointRequestInit): Promise<Dependency> {
  return client.call('NonNullApiEndpoint', 'defaultMethod', { param }, init);
}

export async function genericMethod(generic: Generic<string>, init?: EndpointRequestInit): Promise<Generic<string>> {
  return client.call('NonNullApiEndpoint', 'genericMethod', { generic }, init);
}

export async function genericNullableMethod(
  generic: Generic<string | undefined>,
  init?: EndpointRequestInit,
): Promise<Generic<string | undefined>> {
  return client.call('NonNullApiEndpoint', 'genericNullableMethod', { generic }, init);
}

export async function nestedSignatureMethod(
  param: Array<Dependency>,
  init?: EndpointRequestInit,
): Promise<Record<string, Array<Dependency>>> {
  return client.call('NonNullApiEndpoint', 'nestedSignatureMethod', { param }, init);
}

export async function nullableMethod(
  param: string | undefined,
  init?: EndpointRequestInit,
): Promise<Dependency | undefined> {
  return client.call('NonNullApiEndpoint', 'nullableMethod', { param }, init);
}

export async function nullableNestedSignatureMethod(
  param: Array<Dependency | undefined>,
  init?: EndpointRequestInit,
): Promise<Record<string, Array<Dependency | undefined> | undefined>> {
  return client.call('NonNullApiEndpoint', 'nullableNestedSignatureMethod', { param }, init);
}

export async function nullableSignature(
  param: string | undefined,
  init?: EndpointRequestInit,
): Promise<Dependency | undefined> {
  return client.call('NonNullApiEndpoint', 'nullableSignature', { param }, init);
}

export async function optionalMethod(opt: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
  return client.call('NonNullApiEndpoint', 'optionalMethod', { opt }, init);
}

export async function subPackageMethod(
  entity: SubPackageDependency,
  init?: EndpointRequestInit,
): Promise<SubPackageDependency> {
  return client.call('NonNullApiEndpoint', 'subPackageMethod', { entity }, init);
}
