import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Sort from "./com/vaadin/hilla/mappedtypes/Sort.js";
import client from "./connect-client.default.js";
async function getSort(init?: EndpointRequestInit): Promise<Sort | undefined> { return client.call("BarePageableEndpoint", "getSort", {}, init); }
export { getSort };
