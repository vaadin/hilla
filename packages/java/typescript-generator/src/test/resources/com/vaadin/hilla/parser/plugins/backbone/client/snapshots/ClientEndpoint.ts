import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function greet(name: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> { return client.call("ClientEndpoint", "greet", { name }, init); }
export { greet };
