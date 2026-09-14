import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { MessageInput } from '@vaadin/react-components/MessageInput.js';
import { MessageList } from '@vaadin/react-components/MessageList.js';
import { useEffect, useState } from 'react';
import type Message from 'Frontend/generated/com/example/application/endpoints/ChatService/Message.js';
import { ChatService } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
  title: 'Chat',
  loginRequired: true,
};

export default function ChatView(): React.JSX.Element {
  const [messages, setMessages] = useState<Message[]>([]);

  useEffect(() => {
    const subscription = ChatService.join().onNext((message) => {
      setMessages((previous) => [...previous, message]);
    });

    return () => subscription.cancel();
  }, []);

  return (
    <div className="flex flex-col h-full">
      <MessageList className="flex-grow" items={messages} />
      <MessageInput onSubmit={(e) => ChatService.send(e.detail.value)} />
    </div>
  );
}
