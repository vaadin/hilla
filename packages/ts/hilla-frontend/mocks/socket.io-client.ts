interface Socket {}

export const io = (path: string, options: any): Socket => {
  return {
    on: (type: string, event: any) => {},
    send: (...args: any[]) => {},
  };
};
