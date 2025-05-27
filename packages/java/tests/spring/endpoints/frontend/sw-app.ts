/// <reference lib="webworker" />

import {AppEndpoint} from "Frontend/generated/endpoints";

declare var self: ServiceWorkerGlobalScope;

async function main() {
  const hello = await AppEndpoint.helloAnonymous();
  const clients = await self.clients.matchAll({ type: "window" });
  for (const client of clients) {
    client.postMessage({
      type: 'sw-app-hello',
      hello,
    });
  }
}
// TODO: enable endpoints and call main()
Promise.reject().then(main, () => {});
