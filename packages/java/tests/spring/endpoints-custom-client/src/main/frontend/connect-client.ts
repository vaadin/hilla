import { ConnectClient, type MiddlewareContext, type MiddlewareNext } from '@vaadin/hilla-frontend';

async function logger(context: MiddlewareContext, next?: MiddlewareNext): Promise<Response> {
  const start = new Date().getTime();
  try {
    return (await next?.(context)) as Response;
  } finally {
    const duration = new Date().getTime() - start;
    const message = `[LOG] ${context.endpoint}/${context.method} took ${duration} ms`;
    // eslint-disable-next-line no-console
    console.log(message);
    document.querySelector('#log')!.append(message);
  }
}

const client = new ConnectClient({ middlewares: [logger], prefix: 'connect' });
export default client;
