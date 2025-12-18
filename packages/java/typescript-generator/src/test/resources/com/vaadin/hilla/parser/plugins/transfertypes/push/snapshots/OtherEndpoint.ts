import { EndpointRequestInit as EndpointRequestInit_1, Subscription as Subscription_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
function getMessageFlux_1(count: number): Subscription_1<string | undefined> { return client_1.subscribe("OtherEndpoint", "getMessageFlux", { count }); }
async function toUpperCase_1(message: string | undefined, init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("OtherEndpoint", "toUpperCase", { message }, init); }
export { getMessageFlux_1 as getMessageFlux, toUpperCase_1 as toUpperCase };
