import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ListSignal as ListSignal_1, NumberSignal as NumberSignal_1, Signal as Signal_1, ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
async function getNumberSignal_1(init?: EndpointRequestInit_1): Promise<NumberSignal_1 | undefined> { return client_1.call("SignalEndpoint", "getNumberSignal", {}, init); }
async function getStringListSignal_1(init?: EndpointRequestInit_1): Promise<ListSignal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringListSignal", {}, init); }
async function getStringSignal_1(init?: EndpointRequestInit_1): Promise<Signal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringSignal", {}, init); }
async function getStringValueSignal_1(init?: EndpointRequestInit_1): Promise<ValueSignal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringValueSignal", {}, init); }
export { getNumberSignal_1 as getNumberSignal, getStringListSignal_1 as getStringListSignal, getStringSignal_1 as getStringSignal, getStringValueSignal_1 as getStringValueSignal };
