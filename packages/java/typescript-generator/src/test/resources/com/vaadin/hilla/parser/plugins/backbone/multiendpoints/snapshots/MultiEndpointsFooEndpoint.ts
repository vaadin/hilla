import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type MultiEndpointsSharedModel from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
import client from "./connect-client.default.js";
async function getFoo(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("MultiEndpointsFooEndpoint", "getFoo", {}, init); }
async function getShared(init?: EndpointRequestInit): Promise<MultiEndpointsSharedModel | undefined> { return client.call("MultiEndpointsFooEndpoint", "getShared", {}, init); }
export { getFoo, getShared };
