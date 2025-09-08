import { EndpointRequestInit as EndpointRequestInit_1, Order as Order_1, Page as Page_1, Pageable as Pageable_1, Sort as Sort_1 } from "@vaadin/hilla-frontend";
import type Custom_1 from "./com/vaadin/hilla/parser/plugins/transfertypes/pageable/basic/PageableEndpoint/Custom.js";
import client_1 from "./connect-client.default.js";
async function getCustom_1(init?: EndpointRequestInit_1): Promise<Custom_1 | undefined> { return client_1.call("PageableEndpoint", "getCustom", {}, init); }
async function getOrder_1(init?: EndpointRequestInit_1): Promise<Order_1 | undefined> { return client_1.call("PageableEndpoint", "getOrder", {}, init); }
async function getPage_1(init?: EndpointRequestInit_1): Promise<Page_1<string | undefined> | undefined> { return client_1.call("PageableEndpoint", "getPage", {}, init); }
async function getPageable_1(init?: EndpointRequestInit_1): Promise<Pageable_1 | undefined> { return client_1.call("PageableEndpoint", "getPageable", {}, init); }
async function getSlice_1(init?: EndpointRequestInit_1): Promise<Array<string | undefined> | undefined> { return client_1.call("PageableEndpoint", "getSlice", {}, init); }
async function getSort_1(init?: EndpointRequestInit_1): Promise<Sort_1 | undefined> { return client_1.call("PageableEndpoint", "getSort", {}, init); }
export { getCustom_1 as getCustom, getOrder_1 as getOrder, getPage_1 as getPage, getPageable_1 as getPageable, getSlice_1 as getSlice, getSort_1 as getSort };
