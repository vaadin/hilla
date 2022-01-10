import {
  ConnectClient,
  InvalidSessionMiddleware,
} from '@vaadin/fusion-frontend';

const client = new ConnectClient({
  prefix: 'connect',
  middlewares: [
    new InvalidSessionMiddleware(async () => {
      location.reload();
      return {
        error: true
      }
    })
  ],
});

export default client;
