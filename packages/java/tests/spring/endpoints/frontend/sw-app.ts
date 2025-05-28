/// <reference lib="webworker" />

import {AppEndpoint} from "Frontend/generated/endpoints";

declare var self: ServiceWorkerGlobalScope;

self.addEventListener('message', (e: ExtendableMessageEvent) => {
  let endpoint: undefined | (() => Promise<string | undefined>) = undefined;
  if (e.data === 'helloAnonymous') {
    endpoint = AppEndpoint.helloAnonymous;
  }
  if (endpoint) {
    e.waitUntil(endpoint()?.then((result) => {
      e.source?.postMessage({
        type: 'sw-app-message',
        text: `SW message: ${result}`,
      });
    }));
  }
});
