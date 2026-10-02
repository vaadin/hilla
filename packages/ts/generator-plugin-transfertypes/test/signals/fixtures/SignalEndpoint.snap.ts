import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import { ListSignal, NumberSignal, Signal, ValueSignal } from "@vaadin/hilla-react-signals";
import client from "./connect-client.default.js";
async function getNumberSignal(init?: EndpointRequestInit): Promise<NumberSignal | undefined> { return client.call("SignalEndpoint", "getNumberSignal", {}, init); }
async function getStringListSignal(init?: EndpointRequestInit): Promise<ListSignal<string | undefined> | undefined> { return client.call("SignalEndpoint", "getStringListSignal", {}, init); }
async function getStringSignal(init?: EndpointRequestInit): Promise<Signal<string | undefined> | undefined> { return client.call("SignalEndpoint", "getStringSignal", {}, init); }
async function getStringValueSignal(init?: EndpointRequestInit): Promise<ValueSignal<string | undefined> | undefined> { return client.call("SignalEndpoint", "getStringValueSignal", {}, init); }
export { getNumberSignal, getStringListSignal, getStringSignal, getStringValueSignal };
