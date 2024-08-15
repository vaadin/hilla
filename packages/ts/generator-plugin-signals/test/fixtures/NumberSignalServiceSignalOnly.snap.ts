import { NumberSignal as NumberSignal_1, SignalChannel as SignalChannel_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
const channel_1 = new SignalChannel_1(client_1, "NumberSignalService.counter");
function counter_1() {
    return new NumberSignal_1(undefined, { channel: channel_1 });
}
const channel_2 = new SignalChannel_1(client_1, "NumberSignalService.sharedValue");
function sharedValue_1() {
    return new NumberSignal_1(undefined, { channel: channel_2 });
}
export { counter_1 as counter, sharedValue_1 as sharedValue };
