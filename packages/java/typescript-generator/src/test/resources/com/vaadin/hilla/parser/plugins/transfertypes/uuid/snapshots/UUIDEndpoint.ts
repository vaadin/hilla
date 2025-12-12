import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getUUID_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("UUIDEndpoint", "getUUID", {}, init); }
export { getUUID_1 as getUUID };
