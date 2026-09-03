import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Order_1 from "./com/vaadin/hilla/mappedtypes/Order.js";
import type Pageable_1 from "./com/vaadin/hilla/mappedtypes/Pageable.js";
import type Sort_1 from "./com/vaadin/hilla/mappedtypes/Sort.js";
import client_1 from "./connect-client.default.js";
async function getOrder_1(init?: EndpointRequestInit_1): Promise<Order_1 | undefined> { return client_1.call("PageableEndpoint", "getOrder", {}, init); }
async function getPage_1(init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("PageableEndpoint", "getPage", {}, init); }
async function getPageable_1(init?: EndpointRequestInit_1): Promise<Pageable_1 | undefined> { return client_1.call("PageableEndpoint", "getPageable", {}, init); }
async function getSort_1(init?: EndpointRequestInit_1): Promise<Sort_1 | undefined> { return client_1.call("PageableEndpoint", "getSort", {}, init); }
export { getOrder_1 as getOrder, getPage_1 as getPage, getPageable_1 as getPageable, getSort_1 as getSort };
