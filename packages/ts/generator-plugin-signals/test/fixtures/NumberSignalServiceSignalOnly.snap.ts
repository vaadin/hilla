import { createSignalChannel as createSignalChannel_1, NumberSignal as NumberSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function counter_1(): NumberSignal_1 {
    const signal = new NumberSignal_1();
    createSignalChannel_1(signal, "NumberSignalService.counter", client_1);
    return signal;
}
function sharedValue_1(): NumberSignal_1 {
    const signal = new NumberSignal_1();
    createSignalChannel_1(signal, "NumberSignalService.sharedValue", client_1);
    return signal;
}
export { counter_1 as counter, sharedValue_1 as sharedValue };
