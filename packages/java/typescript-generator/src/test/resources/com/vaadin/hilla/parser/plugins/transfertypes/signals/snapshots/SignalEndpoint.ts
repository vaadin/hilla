import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ListSignal as ListSignal_1, NumberSignal as NumberSignal_1, Signal as Signal_1, SignalMethodOptions as SignalMethodOptions_1, ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function getNumberSignal_1(): NumberSignal_1 | undefined { return new NumberSignal_1(0, {
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getNumberSignal"
}); }
function getStringListSignal_1(): ListSignal_1<string | undefined> | undefined { return new ListSignal_1({
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getStringListSignal"
}); }
async function getStringSignal_1(init?: EndpointRequestInit_1): Promise<Signal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringSignal", {}, init); }
function getStringValueSignal_1(options?: SignalMethodOptions_1<string | undefined>): ValueSignal_1<string | undefined> | undefined { return new ValueSignal_1(options?.defaultValue, {
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getStringValueSignal"
}); }
export { getNumberSignal_1 as getNumberSignal, getStringListSignal_1 as getStringListSignal, getStringSignal_1 as getStringSignal, getStringValueSignal_1 as getStringValueSignal };
