import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type ListSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/ListSignal.js";
import type NumberSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/NumberSignal.js";
import type Signal_1 from "./com/vaadin/hilla/runtime/transfertypes/Signal.js";
import type ValueSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/ValueSignal.js";
import client_1 from "./connect-client.default.js";
async function getNumberSignal_1(init?: EndpointRequestInit_1): Promise<NumberSignal_1 | undefined> { return client_1.call("SignalEndpoint", "getNumberSignal", {}, init); }
async function getStringListSignal_1(init?: EndpointRequestInit_1): Promise<ListSignal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringListSignal", {}, init); }
async function getStringSignal_1(init?: EndpointRequestInit_1): Promise<Signal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringSignal", {}, init); }
async function getStringValueSignal_1(init?: EndpointRequestInit_1): Promise<ValueSignal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringValueSignal", {}, init); }
export { getNumberSignal_1 as getNumberSignal, getStringListSignal_1 as getStringListSignal, getStringSignal_1 as getStringSignal, getStringValueSignal_1 as getStringValueSignal };
