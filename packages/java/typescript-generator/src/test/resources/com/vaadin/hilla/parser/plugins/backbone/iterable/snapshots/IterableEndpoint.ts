import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Foo_1 from "./com/vaadin/hilla/parser/plugins/backbone/iterable/IterableEndpoint/Foo.js";
import client_1 from "./connect-client.default.js";
async function getFooAnotherCustomIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooAnotherCustomIterable", {}, init); }
async function getFooArray_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooArray", {}, init); }
async function getFooCustomIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooCustomIterable", {}, init); }
async function getFooIterable_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooIterable", {}, init); }
async function getFooList_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooList", {}, init); }
async function getFooSet_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getFooSet", {}, init); }
async function getSpecializedIterable_1(init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("IterableEndpoint", "getSpecializedIterable", {}, init); }
async function getSpecializedIterableCustom_1(init?: EndpointRequestInit_1): Promise<Array<Foo_1 | undefined> | undefined> { return client_1.call("IterableEndpoint", "getSpecializedIterableCustom", {}, init); }
export { getFooAnotherCustomIterable_1 as getFooAnotherCustomIterable, getFooArray_1 as getFooArray, getFooCustomIterable_1 as getFooCustomIterable, getFooIterable_1 as getFooIterable, getFooList_1 as getFooList, getFooSet_1 as getFooSet, getSpecializedIterable_1 as getSpecializedIterable, getSpecializedIterableCustom_1 as getSpecializedIterableCustom };
