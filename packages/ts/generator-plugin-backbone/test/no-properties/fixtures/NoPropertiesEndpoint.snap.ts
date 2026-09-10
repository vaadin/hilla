import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ExampleEntity from "./com/example/application/entities/ExampleEntity.js";
import client from "./connect-client.default.js";
async function sayHello(id: ExampleEntity, init?: EndpointRequestInit): Promise<string> { return client.call("NoPropertiesEndpoint", "sayHello", { id }, init); }
export { sayHello };
