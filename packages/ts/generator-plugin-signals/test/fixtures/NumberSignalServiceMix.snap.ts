import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { createSignalChannel as createSignalChannel_1, NumberSignal as NumberSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function counter_1(): NumberSignal_1 {
    const signal = new NumberSignal_1();
    createSignalChannel_1(signal, "NumberSignalService.counter", client_1);
    return signal;
}
async function sayHello_1(name: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NumberSignalService", "sayHello", { name }, init); }
function sharedValue_1(): NumberSignal_1 {
    const signal = new NumberSignal_1();
    createSignalChannel_1(signal, "NumberSignalService.sharedValue", client_1);
    return signal;
}
export { counter_1 as counter, sayHello_1 as sayHello, sharedValue_1 as sharedValue };
