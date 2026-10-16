import { Subscription } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
function getMessageFlux(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function getNonNullMessageFlux(): Subscription<string> { return client.subscribe("PushTypeEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getSubscription", {}); }
export { getMessageFlux, getNonNullMessageFlux, getSubscription };
