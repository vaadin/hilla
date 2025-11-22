import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ListSignal as ListSignal_2, NumberSignal as NumberSignal_2, SignalMethodOptions as SignalMethodOptions_1, ValueSignal as ValueSignal_2 } from "@vaadin/hilla-react-signals";
import type ListSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/ListSignal.js";
import type NumberSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/NumberSignal.js";
import type Signal_1 from "./com/vaadin/hilla/runtime/transfertypes/Signal.js";
import type ValueSignal_1 from "./com/vaadin/hilla/runtime/transfertypes/ValueSignal.js";
import client_1 from "./connect-client.default.js";
function getNumberSignal_1(): NumberSignal_1 | undefined { return new NumberSignal_2(0, {
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getNumberSignal"
}); }
function getStringListSignal_1(): ListSignal_1<string | undefined> | undefined { return new ListSignal_2({
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getStringListSignal"
}); }
async function getStringSignal_1(init?: EndpointRequestInit_1): Promise<Signal_1<string | undefined> | undefined> { return client_1.call("SignalEndpoint", "getStringSignal", {}, init); }
function getStringValueSignal_1(options?: SignalMethodOptions_1<string | undefined>): ValueSignal_1<string | undefined> | undefined { return new ValueSignal_2(options?.defaultValue, {
    client: client_1,
    endpoint: "SignalEndpoint",
    method: "getStringValueSignal"
}); }
export { getNumberSignal_1 as getNumberSignal, getStringListSignal_1 as getStringListSignal, getStringSignal_1 as getStringSignal, getStringValueSignal_1 as getStringValueSignal };
