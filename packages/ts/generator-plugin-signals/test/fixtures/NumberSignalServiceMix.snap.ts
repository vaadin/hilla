import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import { NumberSignal } from "@vaadin/hilla-react-signals";
import client from "./connect-client.default.js";
function counter(): NumberSignal { return new NumberSignal(0, {
    client: client,
    endpoint: "NumberSignalService",
    method: "counter"
}); }
async function sayHello(name: string, init?: EndpointRequestInit): Promise<string> { return client.call("NumberSignalService", "sayHello", { name }, init); }
function sharedValue(highOrLow: boolean, date: string | undefined): NumberSignal { return new NumberSignal(0, {
    client: client,
    endpoint: "NumberSignalService",
    method: "sharedValue",
    params: { highOrLow, date }
}); }
export { counter, sayHello, sharedValue };
