import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function client_2(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("ShadowingEndpoint", "client", {}, init); }
async function echo_1(client: string | undefined, init: string | undefined, _init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("ShadowingEndpoint", "echo", { client, init }, _init); }
export { client_2 as client, echo_1 as echo };
