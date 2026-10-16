import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getBareList(init?: EndpointRequestInit): Promise<Array<unknown> | undefined> { return client.call("BareGenericsEndpoint", "getBareList", {}, init); }
async function getBareMap(init?: EndpointRequestInit): Promise<Record<string, unknown> | undefined> { return client.call("BareGenericsEndpoint", "getBareMap", {}, init); }
async function getBareOptional(init?: EndpointRequestInit): Promise<unknown> { return client.call("BareGenericsEndpoint", "getBareOptional", {}, init); }
export { getBareList, getBareMap, getBareOptional };
