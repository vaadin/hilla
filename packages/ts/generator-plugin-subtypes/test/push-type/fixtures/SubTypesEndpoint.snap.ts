import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type BaseEvent from "./com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.js";
import type Notification from "./com/vaadin/hilla/parser/plugins/subtypes/Notification.js";
import type Shape from "./com/vaadin/hilla/parser/plugins/subtypes/Shape.js";
import client from "./connect-client.default.js";
async function receiveEvent(event: BaseEvent | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("SubTypesEndpoint", "receiveEvent", { event }, init); }
async function sendEvent(init?: EndpointRequestInit): Promise<BaseEvent | undefined> { return client.call("SubTypesEndpoint", "sendEvent", {}, init); }
async function sendNotification(init?: EndpointRequestInit): Promise<Notification | undefined> { return client.call("SubTypesEndpoint", "sendNotification", {}, init); }
async function sendShape(init?: EndpointRequestInit): Promise<Shape | undefined> { return client.call("SubTypesEndpoint", "sendShape", {}, init); }
export { receiveEvent, sendEvent, sendNotification, sendShape };
