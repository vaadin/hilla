import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Sort_1 from "./com/vaadin/hilla/mappedtypes/Sort.js";
import client_1 from "./connect-client.default.js";
async function getSort_1(init?: EndpointRequestInit_1): Promise<Sort_1 | undefined> { return client_1.call("BarePageableEndpoint", "getSort", {}, init); }
export { getSort_1 as getSort };
