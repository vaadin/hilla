import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function doSomething_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("NameFromAnnotationExplicitValueEndpoint", "doSomething", {}, init); }
export { doSomething_1 as doSomething };

