import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getSomething(something: unknown, init?: EndpointRequestInit): Promise<unknown> { return client.call("GenericsBareEndpoint", "getSomething", { something }, init); }
export { getSomething };
