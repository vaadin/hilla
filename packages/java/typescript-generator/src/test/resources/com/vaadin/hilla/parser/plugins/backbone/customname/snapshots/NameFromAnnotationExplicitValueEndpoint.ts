import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function doSomething(init?: EndpointRequestInit): Promise<void> { return client.call("NameFromAnnotationExplicitValueEndpoint", "doSomething", {}, init); }
export { doSomething };
