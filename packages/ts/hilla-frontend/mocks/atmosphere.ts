export const atmosphere = {
  opened: false,
  reset: () => {
    atmosphere.opened = false;
  },
  subscribe: (req: any): any => {
    const sentMessages: any[] = [];
    if (atmosphere.opened) {
      throw new Error('Atmosphere subscribe called while already subscribed');
    }
    atmosphere.opened = true;

    const ret = {
      fakeEvent: (event: string, data?: any) => {
        req[event](data);
      },
      push: (...args: any[]) => {
        sentMessages.push(...args);
      },
      sentMessages,
    };
    ret.fakeEvent('onOpen');
    return ret;
  },
};
