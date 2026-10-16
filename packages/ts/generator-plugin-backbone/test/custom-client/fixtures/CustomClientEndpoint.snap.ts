import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "../connect-client.js";
async function getString(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("CustomClientEndpoint", "getString", {}, init); }
export { getString };
