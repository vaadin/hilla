import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getBareList_1(init?: EndpointRequestInit_1): Promise<Array<unknown> | undefined> { return client_1.call("BareTypeEndpoint", "getBareList", {}, init); }
async function getBareMap_1(init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("BareTypeEndpoint", "getBareMap", {}, init); }
async function getBareOptional_1(init?: EndpointRequestInit_1): Promise<unknown | undefined> { return client_1.call("BareTypeEndpoint", "getBareOptional", {}, init); }
export { getBareList_1 as getBareList, getBareMap_1 as getBareMap, getBareOptional_1 as getBareOptional };
