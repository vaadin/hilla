import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Foo from "./com/vaadin/hilla/parser/plugins/backbone/iterable/IterableEndpoint/Foo.js";
import client from "./connect-client.default.js";
async function getFooAnotherCustomIterable(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooAnotherCustomIterable", {}, init); }
async function getFooArray(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooArray", {}, init); }
async function getFooCustomIterable(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooCustomIterable", {}, init); }
async function getFooIterable(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooIterable", {}, init); }
async function getFooList(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooList", {}, init); }
async function getFooSet(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getFooSet", {}, init); }
async function getSpecializedIterable(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> { return client.call("IterableEndpoint", "getSpecializedIterable", {}, init); }
async function getSpecializedIterableCustom(init?: EndpointRequestInit): Promise<Array<Foo | undefined> | undefined> { return client.call("IterableEndpoint", "getSpecializedIterableCustom", {}, init); }
export { getFooAnotherCustomIterable, getFooArray, getFooCustomIterable, getFooIterable, getFooList, getFooSet, getSpecializedIterable, getSpecializedIterableCustom };
