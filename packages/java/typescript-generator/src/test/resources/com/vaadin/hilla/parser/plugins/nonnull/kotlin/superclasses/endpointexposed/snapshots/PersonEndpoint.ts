import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Person_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/superclasses/endpointexposed/PersonEndpoint/Person.js";
import client_1 from "./connect-client.default.js";
async function get_1(id: number, init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonEndpoint", "get", { id }, init); }
async function delete_1(id: number, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("PersonEndpoint", "delete", { id }, init); }
async function save_1(entity: Person_1 | undefined, init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonEndpoint", "save", { entity }, init); }
async function update_1(entity: Person_1, init?: EndpointRequestInit_1): Promise<Person_1> { return client_1.call("PersonEndpoint", "update", { entity }, init); }
async function getNonNullablePage_1(pageSize: number, pageNumber: number, parameters: Record<string, Person_1>, init?: EndpointRequestInit_1): Promise<Array<Person_1>> { return client_1.call("PersonEndpoint", "getNonNullablePage", { pageSize, pageNumber, parameters }, init); }
async function getPage_1(pageSize: number, pageNumber: number, init?: EndpointRequestInit_1): Promise<Array<Person_1>> { return client_1.call("PersonEndpoint", "getPage", { pageSize, pageNumber }, init); }
async function size_1(init?: EndpointRequestInit_1): Promise<number> { return client_1.call("PersonEndpoint", "size", {}, init); }
async function create_1(entity: Person_1, init?: EndpointRequestInit_1): Promise<number> { return client_1.call("PersonEndpoint", "create", { entity }, init); }
export { create_1 as create, delete_1 as delete, get_1 as get, getNonNullablePage_1 as getNonNullablePage, getPage_1 as getPage, save_1 as save, size_1 as size, update_1 as update };
