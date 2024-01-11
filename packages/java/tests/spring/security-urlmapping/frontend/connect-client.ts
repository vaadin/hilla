import { ConnectClient, InvalidSessionMiddleware } from '@vaadin/hilla-core';

const client = new ConnectClient({
  prefix: '../connect',
  middlewares: [
    new InvalidSessionMiddleware(async () => {
      location.reload();
      return {
        error: true,
      };
    }),
  ],
});

export default client;
