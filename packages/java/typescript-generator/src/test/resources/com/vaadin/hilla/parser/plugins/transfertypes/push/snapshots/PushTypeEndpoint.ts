import { Subscription } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
function getMessageFlux(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getMessageFlux", {}); }
function getSubscription(): Subscription<string | undefined> { return client.subscribe("PushTypeEndpoint", "getSubscription", {}); }
export { getMessageFlux, getSubscription };
