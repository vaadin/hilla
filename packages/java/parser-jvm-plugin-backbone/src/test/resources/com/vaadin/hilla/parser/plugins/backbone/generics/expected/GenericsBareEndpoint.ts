import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getSomething_1(something: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("GenericsBareEndpoint", "getSomething", { something }, init); }
export { getSomething_1 as getSomething };

