import { ListSignal } from "@vaadin/hilla-react-signals";
import type Message from "./com/github/taefi/services/ChatService/Message.js";
import client from "./connect-client.default.js";
function chatChannel(): ListSignal<Message> { return new ListSignal({
    client: client,
    endpoint: "ChatService",
    method: "chatChannel"
}); }
function chatChannelByName(channelName: string): ListSignal<Message> { return new ListSignal({
    client: client,
    endpoint: "ChatService",
    method: "chatChannelByName",
    params: { channelName }
}); }
export { chatChannel, chatChannelByName };
