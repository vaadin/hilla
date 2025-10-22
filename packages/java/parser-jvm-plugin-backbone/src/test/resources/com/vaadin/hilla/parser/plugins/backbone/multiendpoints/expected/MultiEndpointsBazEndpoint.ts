import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type MultiEndpointsSharedModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/multiendpoints/MultiEndpointsSharedModel.js";
import client_1 from "./connect-client.default.js";
async function getBaz_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("MultiEndpointsBazEndpoint", "getBaz", {}, init); }
async function getShared_1(init?: EndpointRequestInit_1): Promise<MultiEndpointsSharedModel_1 | undefined> { return client_1.call("MultiEndpointsBazEndpoint", "getShared", {}, init); }
export { getBaz_1 as getBaz, getShared_1 as getShared };

