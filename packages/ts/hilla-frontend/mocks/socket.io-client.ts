interface Socket {}

export const io = (path: string, options: any): Socket => {
  const sentMessages = [];
  const incomingMessages = [];
  return {
    on: (type: string, event: any) => {
      incomingMessages.push({ type, event });
    },
    send: (...args: any[]) => {
      sentMessages.push(...args);
    },
    sentMessages,
    incomingMessages,
  };
};
