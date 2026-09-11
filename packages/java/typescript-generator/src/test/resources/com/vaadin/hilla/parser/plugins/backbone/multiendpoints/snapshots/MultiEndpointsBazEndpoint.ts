import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type MultiEndpointsSharedModel from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
import client from "./connect-client.default.js";
async function getBaz(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("MultiEndpointsBazEndpoint", "getBaz", {}, init); }
async function getShared(init?: EndpointRequestInit): Promise<MultiEndpointsSharedModel | undefined> { return client.call("MultiEndpointsBazEndpoint", "getShared", {}, init); }
export { getBaz, getShared };
