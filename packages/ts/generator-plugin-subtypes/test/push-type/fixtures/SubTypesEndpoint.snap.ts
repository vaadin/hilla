import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-core";
import type BaseEvent_1 from "./com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.js";
import client_1 from "./connect-client.default.js";
async function receiveEvent_1(event: BaseEvent_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("SubTypesEndpoint", "receiveEvent", { event }, init); }
async function sendEvent_1(init?: EndpointRequestInit_1): Promise<BaseEvent_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendEvent", {}, init); }
export { receiveEvent_1 as receiveEvent, sendEvent_1 as sendEvent };
