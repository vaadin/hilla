import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Order from "./com/vaadin/hilla/mappedtypes/Order.js";
import type Pageable from "./com/vaadin/hilla/mappedtypes/Pageable.js";
import type Sort from "./com/vaadin/hilla/mappedtypes/Sort.js";
import client from "./connect-client.default.js";
async function getOrder(init?: EndpointRequestInit): Promise<Order | undefined> { return client.call("PageableEndpoint", "getOrder", {}, init); }
async function getPage(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> { return client.call("PageableEndpoint", "getPage", {}, init); }
async function getPageable(init?: EndpointRequestInit): Promise<Pageable | undefined> { return client.call("PageableEndpoint", "getPageable", {}, init); }
async function getSort(init?: EndpointRequestInit): Promise<Sort | undefined> { return client.call("PageableEndpoint", "getSort", {}, init); }
export { getOrder, getPage, getPageable, getSort };
