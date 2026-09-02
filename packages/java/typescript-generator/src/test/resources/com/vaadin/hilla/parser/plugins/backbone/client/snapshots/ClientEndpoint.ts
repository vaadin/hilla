import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function greet_1(name: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("ClientEndpoint", "greet", { name }, init); }
export { greet_1 as greet };
