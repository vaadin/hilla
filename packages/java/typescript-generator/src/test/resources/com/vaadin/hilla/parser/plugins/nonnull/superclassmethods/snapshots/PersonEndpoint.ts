import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Person from "./com/vaadin/hilla/parser/plugins/nonnull/superclassmethods/PersonEndpoint/Person.js";
import client from "./connect-client.default.js";
async function get(id: number | undefined, init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PersonEndpoint", "get", { id }, init); }
async function delete(id: number | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("PersonEndpoint", "delete", { id }, init); }
async function update(entity: Person | undefined, init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PersonEndpoint", "update", { entity }, init); }
async function getNonNullablePage(pageSize: number, pageNumber: number, parameters: Record<string, Person> | undefined, init?: EndpointRequestInit): Promise<Array<Person> | undefined> { return client.call("PersonEndpoint", "getNonNullablePage", { pageSize, pageNumber, parameters }, init); }
async function getPage(pageSize: number, pageNumber: number, init?: EndpointRequestInit): Promise<Array<Person | undefined> | undefined> { return client.call("PersonEndpoint", "getPage", { pageSize, pageNumber }, init); }
async function size(init?: EndpointRequestInit): Promise<number> { return client.call("PersonEndpoint", "size", {}, init); }
export { delete, get, getNonNullablePage, getPage, size, update };
