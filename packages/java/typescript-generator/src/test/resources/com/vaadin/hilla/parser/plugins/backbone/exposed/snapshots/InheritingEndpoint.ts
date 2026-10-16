import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function shared_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("InheritingEndpoint", "shared", {}, init); }
async function twice_1(one: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("InheritingEndpoint", "twice", { one }, init); }
export { shared_1 as shared, twice_1 as twice };
