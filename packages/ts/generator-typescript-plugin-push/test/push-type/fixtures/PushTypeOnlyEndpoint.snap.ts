import { Subscription as Subscription_1 } from "@hilla/frontend";
import client_1 from "./connect-client.default.js";
function getMessageFlux_1(): Subscription_1<string | undefined> { return client_1.subscribe("PushTypeOnlyEndpoint", "getMessageFlux", {}); }
function getNonNullMessageFlux_1(): Subscription_1<string> { return client_1.subscribe("PushTypeOnlyEndpoint", "getNonNullMessageFlux", {}); }
function getSubscription_1(): Subscription_1<string | undefined> { return client_1.subscribe("PushTypeOnlyEndpoint", "getSubscription", {}); }
export { getMessageFlux_1 as getMessageFlux, getNonNullMessageFlux_1 as getNonNullMessageFlux, getSubscription_1 as getSubscription };
