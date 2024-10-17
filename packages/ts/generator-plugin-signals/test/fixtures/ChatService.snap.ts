import { ListSignal as ListSignal_1 } from "@vaadin/hilla-react-signals";
import type Message_1 from "./com/github/taefi/services/ChatService/Message.js";
import client_1 from "./connect-client.default.js";
function chatChannel_1(): ListSignal_1<Message_1> {
    return new ListSignal_1({ client: client_1, endpoint: "ChatService", method: "chatChannel" });
}
function chatChannelByName_1(channelName: string): ListSignal_1<Message_1> {
    return new ListSignal_1({ client: client_1, endpoint: "ChatService", method: "chatChannelByName", params: { channelName } });
}
export { chatChannel_1 as chatChannel, chatChannelByName_1 as chatChannelByName };
