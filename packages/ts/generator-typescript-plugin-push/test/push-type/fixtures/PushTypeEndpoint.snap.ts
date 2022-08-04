import { EndpointRequestInit as EndpointRequestInit_1, Subscription as Subscription_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default";
function getMessageFlux_1(): Subscription_1<string | undefined> | undefined { return client_1.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function getSubscription_1(): Subscription_1<string | undefined> | undefined { return client_1.subscribe("PushTypeEndpoint", "getSubscription", {}); }
export { getMessageFlux_1 as getMessageFlux, getSubscription_1 as getSubscription };
