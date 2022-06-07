interface Socket {}

export const io = (path: string, options: any): Socket => {
  const sentMessages = [];
  const eventHandlers = {};
  return {
    on: (event: string, listener: any) => {
      if (!eventHandlers[event]) {
        eventHandlers[event] = [];
      }
      eventHandlers[event].push(listener);
    },
    emit: (event: string, ...args: any[]) => {
      (eventHandlers[event] || []).forEach((l: any) => l(args));
    },
    send: (...args: any[]) => {
      sentMessages.push(...args);
    },
    sentMessages,
  };
};
