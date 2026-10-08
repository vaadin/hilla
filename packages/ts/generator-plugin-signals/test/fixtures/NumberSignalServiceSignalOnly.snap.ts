import { NumberSignal } from "@vaadin/hilla-react-signals";
import client from "./connect-client.default.js";
function counter(): NumberSignal { return new NumberSignal(0, {
    client: client,
    endpoint: "NumberSignalService",
    method: "counter"
}); }
function sharedValue(): NumberSignal { return new NumberSignal(0, {
    client: client,
    endpoint: "NumberSignalService",
    method: "sharedValue"
}); }
export { counter, sharedValue };
