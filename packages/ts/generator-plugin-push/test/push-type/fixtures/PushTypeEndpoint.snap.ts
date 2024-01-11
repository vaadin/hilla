import { EndpointRequestInit as EndpointRequestInit_1, Subscription as Subscription_1 } from "@vaadin/hilla-core";
import client_1 from "./connect-client.default.js";
function getMessageFlux_1(): Subscription_1<string | undefined> { return client_1.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function getNonNullMessageFlux_1(): Subscription_1<string> { return client_1.subscribe("PushTypeEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription_1(): Subscription_1<string | undefined> { return client_1.subscribe("PushTypeEndpoint", "getSubscription", {}); }
async function notRelatedToPush_1(arg: string, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("PushTypeEndpoint", "notRelatedToPush", { arg }, init); }
export { getMessageFlux_1 as getMessageFlux, getNonNullMessageFlux_1 as getNonNullMessageFlux, getSubscription_1 as getSubscription, notRelatedToPush_1 as notRelatedToPush };
