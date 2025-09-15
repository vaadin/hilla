import { ConnectClient, InvalidSessionMiddleware } from '@vaadin/hilla-frontend';

const client = new ConnectClient({
  prefix: 'connect',
  middlewares: [
    new InvalidSessionMiddleware(async () => {
      // @ts-ignore
      window.reloadPending = true;
      location.reload();
      return {
        error: true,
      };
    }),
  ],
});

export default client;
