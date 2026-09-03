import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function childMethod1_1(name: string | undefined, age: number | undefined, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("ChildEndpoint", "childMethod1", { name, age }, init); }
export { childMethod1_1 as childMethod1 };
