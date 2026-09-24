import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import { ListSignal, NumberSignal, Signal, SignalMethodOptions, ValueSignal } from "@vaadin/hilla-react-signals";
import client from "./connect-client.default.js";
function getNumberSignal(): NumberSignal | undefined { return new NumberSignal(0, {
    client: client,
    endpoint: "SignalEndpoint",
    method: "getNumberSignal"
}); }
function getStringListSignal(): ListSignal<string | undefined> | undefined { return new ListSignal({
    client: client,
    endpoint: "SignalEndpoint",
    method: "getStringListSignal"
}); }
async function getStringSignal(init?: EndpointRequestInit): Promise<Signal<string | undefined> | undefined> { return client.call("SignalEndpoint", "getStringSignal", {}, init); }
function getStringValueSignal(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> | undefined { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "SignalEndpoint",
    method: "getStringValueSignal"
}); }
export { getNumberSignal, getStringListSignal, getStringSignal, getStringValueSignal };
