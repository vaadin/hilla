import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Sample from "./com/vaadin/hilla/parser/plugins/backbone/jackson/JacksonEndpoint/Sample.js";
import client from "./connect-client.default.js";
async function getSample(init?: EndpointRequestInit): Promise<Sample | undefined> { return client.call("JacksonEndpoint", "getSample", {}, init); }
export { getSample };
