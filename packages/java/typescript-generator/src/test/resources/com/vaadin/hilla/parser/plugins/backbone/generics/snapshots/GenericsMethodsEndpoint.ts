import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type GenericsBareRefEntity from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import client from "./connect-client.default.js";
async function getList(list: Array<string | undefined> | undefined, init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> { return client.call("GenericsMethodsEndpoint", "getList", { list }, init); }
async function getRef(ref: GenericsBareRefEntity<string | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsBareRefEntity<string | undefined> | undefined> { return client.call("GenericsMethodsEndpoint", "getRef", { ref }, init); }
async function getValueWithGenericType(something: unknown, init?: EndpointRequestInit): Promise<unknown> { return client.call("GenericsMethodsEndpoint", "getValueWithGenericType", { something }, init); }
export { getList, getRef, getValueWithGenericType };
