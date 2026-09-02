import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Sample_1 from "./com/vaadin/hilla/parser/plugins/backbone/jackson/JacksonEndpoint/Sample.js";
import client_1 from "./connect-client.default.js";
async function getSample_1(init?: EndpointRequestInit_1): Promise<Sample_1 | undefined> { return client_1.call("JacksonEndpoint", "getSample", {}, init); }
export { getSample_1 as getSample };
