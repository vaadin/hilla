import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getUUID(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("UUIDEndpoint", "getUUID", {}, init); }
export { getUUID };
