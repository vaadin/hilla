import { Subscription } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
function getMessageFlux(): Subscription<string | undefined> { return client.subscribe("PushTypeOnlyEndpoint", "getMessageFlux", {}); }
function getNonNullMessageFlux(): Subscription<string> { return client.subscribe("PushTypeOnlyEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription(EndpointRequestInit: string): Subscription<string | undefined> { return client.subscribe("PushTypeOnlyEndpoint", "getSubscription", { EndpointRequestInit }); }
export { getMessageFlux, getNonNullMessageFlux, getSubscription };
