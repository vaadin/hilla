import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type MultiEndpointsSharedModel from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
import client from "./connect-client.default.js";
async function getBar(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("MultiEndpointsBarEndpoint", "getBar", {}, init); }
async function getShared(init?: EndpointRequestInit): Promise<MultiEndpointsSharedModel | undefined> { return client.call("MultiEndpointsBarEndpoint", "getShared", {}, init); }
export { getBar, getShared };
