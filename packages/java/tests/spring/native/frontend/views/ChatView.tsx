import {Button} from "@hilla/react-components/Button.js";
import {logout} from "@hilla/frontend";
import {MessageList} from "@hilla/react-components/MessageList.js";
import {MessageInput} from "@hilla/react-components/MessageInput.js";
import {useEffect, useState} from "react";
import Message from "Frontend/generated/com/example/application/endpoints/ChatService/Message";
import {ChatService} from "Frontend/generated/endpoints.js";

export function ChatView() {
  const [messages, setMessages] = useState<Message[]>([]);

  useEffect(() => {
    const sub = ChatService.join().onNext(message => {
      setMessages(prevState => [...prevState, message]);
    });

    return () => sub.cancel();
  },[]);

  return (
    <div className="flex flex-col h-full">
      <Button onClick={() => logout()} className="self-end">Log out</Button>
      <MessageList className="flex-grow" items={messages}/>
      <MessageInput onSubmit={e => ChatService.send(e.detail.value)}/>
    </div>
  )
}
