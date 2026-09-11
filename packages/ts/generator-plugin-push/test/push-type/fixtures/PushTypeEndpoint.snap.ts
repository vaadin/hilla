import { EndpointRequestInit, Subscription } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
function getMessageFlux(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function delete_1(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "delete", {}); }
function getNonNullMessageFlux(): Subscription<string> { return client.subscribe("PushTypeEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getSubscription", {}); }
async function notRelatedToPush(arg: string, init?: EndpointRequestInit): Promise<void> { return client.call("PushTypeEndpoint", "notRelatedToPush", { arg }, init); }
export { delete_1 as delete, getMessageFlux, getNonNullMessageFlux, getSubscription, notRelatedToPush };
