import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function childMethod1(name: string | undefined, age: number | undefined, init?: EndpointRequestInit): Promise<string> { return client.call("ChildEndpoint", "childMethod1", { name, age }, init); }
export { childMethod1 };
