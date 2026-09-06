import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type BaseEvent_1 from "./com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.js";
import type Job_1 from "./com/vaadin/hilla/parser/plugins/subtypes/Job.js";
import type Notification_1 from "./com/vaadin/hilla/parser/plugins/subtypes/Notification.js";
import type Payload_1 from "./com/vaadin/hilla/parser/plugins/subtypes/Payload.js";
import type Shape_1 from "./com/vaadin/hilla/parser/plugins/subtypes/Shape.js";
import client_1 from "./connect-client.default.js";
async function receiveEvent_1(event: BaseEvent_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("SubTypesEndpoint", "receiveEvent", { event }, init); }
async function sendEvent_1(init?: EndpointRequestInit_1): Promise<BaseEvent_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendEvent", {}, init); }
async function sendJob_1(init?: EndpointRequestInit_1): Promise<Job_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendJob", {}, init); }
async function sendNotification_1(init?: EndpointRequestInit_1): Promise<Notification_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendNotification", {}, init); }
async function sendPayload_1(init?: EndpointRequestInit_1): Promise<Payload_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendPayload", {}, init); }
async function sendShape_1(init?: EndpointRequestInit_1): Promise<Shape_1 | undefined> { return client_1.call("SubTypesEndpoint", "sendShape", {}, init); }
export { receiveEvent_1 as receiveEvent, sendEvent_1 as sendEvent, sendJob_1 as sendJob, sendNotification_1 as sendNotification, sendPayload_1 as sendPayload, sendShape_1 as sendShape };
