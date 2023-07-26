import { EndpointRequestInit as EndpointRequestInit_1 } from "@hilla/frontend";
import type ExampleEntity_1 from "./com/example/application/entities/ExampleEntity.js";
import client_1 from "./connect-client.default.js";
async function sayHello_1(id: ExampleEntity_1, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NoPropertiesEndpoint", "sayHello", { id }, init); }
export { sayHello_1 as sayHello };
