import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function sayHello(name: string, init?: EndpointRequestInit): Promise<string> { return client.call("HelloWorldService", "sayHello", { name }, init); }
export { sayHello };
