import { EndpointRequestInit, Subscription } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
function getMessageFlux(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function getNonNullMessageFlux(): Subscription<string> { return client.subscribe("PushTypeEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getSubscription", {}); }
async function notRelatedToPush(arg: string, init?: EndpointRequestInit): Promise<void> { return client.call("PushTypeEndpoint", "notRelatedToPush", { arg }, init); }
export { getMessageFlux, getNonNullMessageFlux, getSubscription, notRelatedToPush };
