import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function client(init?: EndpointRequestInit): Promise<string | undefined> { return client_1.call("ShadowingEndpoint", "client", {}, init); }
async function echo(client: string | undefined, init: string | undefined, _init?: EndpointRequestInit): Promise<string | undefined> { return client_1.call("ShadowingEndpoint", "echo", { client, init }, _init); }
export { client, echo };
