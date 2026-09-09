import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Person from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/superclasses/endpointexposed/PersonEndpoint/Person.js";
import client from "./connect-client.default.js";
async function get(id: number, init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PersonEndpoint", "get", { id }, init); }
async function delete(id: number, init?: EndpointRequestInit): Promise<void> { return client.call("PersonEndpoint", "delete", { id }, init); }
async function save(entity: Person | undefined, init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PersonEndpoint", "save", { entity }, init); }
async function update(entity: Person, init?: EndpointRequestInit): Promise<Person> { return client.call("PersonEndpoint", "update", { entity }, init); }
async function getNonNullablePage(pageSize: number, pageNumber: number, parameters: Record<string, Person>, init?: EndpointRequestInit): Promise<Array<Person>> { return client.call("PersonEndpoint", "getNonNullablePage", { pageSize, pageNumber, parameters }, init); }
async function getPage(pageSize: number, pageNumber: number, init?: EndpointRequestInit): Promise<Array<Person>> { return client.call("PersonEndpoint", "getPage", { pageSize, pageNumber }, init); }
async function size(init?: EndpointRequestInit): Promise<number> { return client.call("PersonEndpoint", "size", {}, init); }
async function create(entity: Person, init?: EndpointRequestInit): Promise<number> { return client.call("PersonEndpoint", "create", { entity }, init); }
export { create, delete, get, getNonNullablePage, getPage, save, size, update };
