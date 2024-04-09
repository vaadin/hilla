import { register } from 'node:module';
import { MessageChannel } from 'node:worker_threads';

function createMessageChannel(
  callback = (msg) => {
    console.log(msg);
  },
) {
  const { port1, port2 } = new MessageChannel();
  port1.on('message', callback);
  return port2;
}

const portMocks = createMessageChannel();

register(new URL('./hooks/mocks.js', import.meta.url), {
  parentURL: import.meta.url,
  data: { number: 2, port: portMocks },
  transferList: [portMocks],
});

const portTsx = createMessageChannel();

register(import.meta.resolve('tsx'), {
  parentURL: import.meta.url,
  data: { number: 1, port: portTsx },
  transferList: [portTsx],
});
