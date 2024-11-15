/// <reference lib="webworker" />

//Copy from generated target/sw.ts with extra import ./sw-extended.js

importScripts('sw-runtime-resources-precache.js');
import './sw-extended.js';
import { clientsClaim, cacheNames, WorkboxPlugin } from 'workbox-core';
import { matchPrecache, precacheAndRoute, getCacheKeyForURL } from 'workbox-precaching';
import { NavigationRoute, registerRoute } from 'workbox-routing';
import { PrecacheEntry } from 'workbox-precaching/_types';
import { NetworkOnly, NetworkFirst } from 'workbox-strategies';

declare var self: ServiceWorkerGlobalScope & {
  __WB_MANIFEST: Array<PrecacheEntry>;
  additionalManifestEntries?: Array<PrecacheEntry>;
};

self.skipWaiting();
clientsClaim();

declare var OFFLINE_PATH: string; // defined by Webpack/Vite
declare var VITE_ENABLED: boolean; // defined by Webpack/Vite

// Combine manifest entries injected at compile-time by Webpack/Vite
// with ones that Flow injects at runtime through `sw-runtime-resources-precache.js`.
let manifestEntries: PrecacheEntry[] = self.__WB_MANIFEST || [];
// If injected entries contains element for root, then discard the one from Flow
// may only happen when running in development mode, but after a frontend build
let hasRootEntry = manifestEntries.findIndex((entry) => entry.url === '.') >= 0;
if (self.additionalManifestEntries?.length) {
  manifestEntries.push(...self.additionalManifestEntries.filter( (entry) => entry.url !== '.' || !hasRootEntry));
}

const offlinePath = OFFLINE_PATH;

// Compute the registration scope path.
// Example: http://localhost:8888/scope-path/sw.js => /scope-path/
const scope = new URL(self.registration.scope);

/**
 * Replaces <base href> in pre-cached response HTML with the service worker’s
 * scope URL.
 *
 * @param response HTML response to modify
 * @returns modified response
 */
async function rewriteBaseHref(response: Response) {
  const html = await response.text();
  return new Response(html.replace(/<base\s+href=[^>]*>/, `<base href="${self.registration.scope}">`), response);
};

/**
 * Returns true if the given URL is included in the manifest, otherwise false.
 */
function isManifestEntryURL(url: URL) {
  return manifestEntries.some((entry) => getCacheKeyForURL(entry.url) === getCacheKeyForURL(`${url}`));
}

/**
 * A workbox plugin that checks and updates the network connection status
 * on every fetch request.
 */
let connectionLost = false;
function checkConnectionPlugin(): WorkboxPlugin {
  return {
    async fetchDidFail() {
      connectionLost = true;
    },
    async fetchDidSucceed({ response }) {
      connectionLost = false;
      return response
    }
  }
}

const networkOnly = new NetworkOnly({
  plugins: [checkConnectionPlugin()]
});
const networkFirst = new NetworkFirst({
  plugins: [checkConnectionPlugin()]
});

if (process.env.NODE_ENV === 'development' && VITE_ENABLED) {
  self.addEventListener('activate', (event) => {
    event.waitUntil(caches.delete(cacheNames.runtime));
  });

  // Cache /VAADIN/* resources in dev mode. Ensure the Vite specific URLs on another port are not handled to avoid excessive logging.
  registerRoute(
    ({ url }) => url.port === scope.port && url.pathname.startsWith(`${scope.pathname}VAADIN/`),
    networkFirst
  );
}

registerRoute(
  new NavigationRoute(async (context) => {
    async function serveOfflineFallback() {
      const response = await matchPrecache(offlinePath);
      return response ? rewriteBaseHref(response) : undefined;
    }

    function serveResourceFromCache() {
      // Always serve the offline fallback at the scope path.
      if (context.url.pathname === scope.pathname) {
        return serveOfflineFallback();
      }

      if (isManifestEntryURL(context.url)) {
        return matchPrecache(context.request);
      }

      return serveOfflineFallback();
    };

    // Try to serve the resource from the cache when offline is detected.
    if (!self.navigator.onLine) {
      const response = await serveResourceFromCache();
      if (response) {
        return response;
      }
    }

    // Sometimes navigator.onLine is not reliable,
    // try to serve the resource from the cache also in the case of a network failure.
    try {
      return await networkOnly.handle(context);
    } catch (error) {
      const response = await serveResourceFromCache();
      if (response) {
        return response;
      }
      throw error;
    }
  })
);

precacheAndRoute(manifestEntries);

self.addEventListener('message', (event) => {
  if (typeof event.data !== 'object' || !('method' in event.data)) {
    return;
  }

  // JSON-RPC request handler for ConnectionStateStore
  if (event.data.method === 'Vaadin.ServiceWorker.isConnectionLost' && 'id' in event.data) {
    event.source?.postMessage({ id: event.data.id, result: connectionLost }, []);
  }
});

// Handle web push

self.addEventListener('push', (e) => {
  const data = e.data?.json();
  if (data) {
    self.registration.showNotification(data.title, {
      body: data.body,
    });
  }
});

self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  e.waitUntil(focusOrOpenWindow());
});

async function focusOrOpenWindow() {
  const url = new URL('/', self.location.origin).href;

  const allWindows = await self.clients.matchAll({
    type: 'window',
  });
  const appWindow = allWindows.find((w) => w.url === url);

  if (appWindow) {
    return appWindow.focus();
  } else {
    return self.clients.openWindow(url);
  }
}
