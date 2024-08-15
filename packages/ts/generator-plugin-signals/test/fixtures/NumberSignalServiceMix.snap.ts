import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { NumberSignal as NumberSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function counter_1() {
    return new NumberSignal_1(undefined, { client: client_1, endpoint: "NumberSignalService", method: "counter" });
}
async function sayHello_1(name: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NumberSignalService", "sayHello", { name }, init); }
function sharedValue_1() {
    return new NumberSignal_1(undefined, { client: client_1, endpoint: "NumberSignalService", method: "sharedValue" });
}
export { counter_1 as counter, sayHello_1 as sayHello, sharedValue_1 as sharedValue };
