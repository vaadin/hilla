import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import client_1 from "./connect-client.default.js";
async function getList_1(list: Array<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("GenericsMethodsEndpoint", "getList", { list }, init); }
async function getRef_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsMethodsEndpoint", "getRef", { ref }, init); }
async function getValueWithGenericType_1(something: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("GenericsMethodsEndpoint", "getValueWithGenericType", { something }, init); }
export { getList_1 as getList, getRef_1 as getRef, getValueWithGenericType_1 as getValueWithGenericType };

