import { ConnectClient, InvalidSessionMiddleware } from '@vaadin/hilla-frontend';

const client = new ConnectClient({
  prefix: 'connect',
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
