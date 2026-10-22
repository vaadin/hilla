import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function doSomething(init?: EndpointRequestInit): Promise<void> { return client.call("NameFromAnnotationEndpoint", "doSomething", {}, init); }
export { doSomething };
