import { NumberSignal as NumberSignal_1, SignalChannel as SignalChannel_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
(function counter_1(this: SignalChannel_1) {
    return new NumberSignal_1(undefined, { channel: this });
}).bind(new SignalChannel_1(client_1, "NumberSignalService.counter"));
(function sharedValue_1(this: SignalChannel_1) {
    return new NumberSignal_1(undefined, { channel: this });
}).bind(new SignalChannel_1(client_1, "NumberSignalService.sharedValue"));
export { counter_1 as counter, sharedValue_1 as sharedValue };
