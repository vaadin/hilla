import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { type NumberSignal as NumberSignal_1, NumberSignalChannel as NumberSignalChannel_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function counter_1(): NumberSignal_1 { return new NumberSignalChannel_1("NumberSignalService.counter", client_1).signal; }
async function sayHello_1(name: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("NumberSignalService", "sayHello", { name }, init); }
function sharedValue_1(): NumberSignal_1 { return new NumberSignalChannel_1("NumberSignalService.sharedValue", client_1).signal; }
export { counter_1 as counter, sayHello_1 as sayHello, sharedValue_1 as sharedValue };
