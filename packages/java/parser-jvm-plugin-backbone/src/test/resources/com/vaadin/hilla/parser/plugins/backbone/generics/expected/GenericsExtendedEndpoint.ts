import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getMap_1(map: Record<string, unknown> | undefined, init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("GenericsExtendedEndpoint", "getMap", { map }, init); }
export { getMap_1 as getMap };

