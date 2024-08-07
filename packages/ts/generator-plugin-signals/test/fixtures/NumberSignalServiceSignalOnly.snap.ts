import { type NumberSignal as NumberSignal_1, NumberSignalChannel as NumberSignalChannel_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function counter_1(): NumberSignal_1 { return new NumberSignalChannel_1("NumberSignalService.counter", client_1).signal; }
function sharedValue_1(): NumberSignal_1 { return new NumberSignalChannel_1("NumberSignalService.sharedValue", client_1).signal; }
export { counter_1 as counter, sharedValue_1 as sharedValue };
