import type { Subscription } from '@vaadin/hilla-frontend';
import client from './connect-client.default.js';

export function getMessageFlux(): Subscription<string | undefined> {
  return client.subscribe('PushTypeEndpoint', 'getMessageFlux', {});
}

export function getNonNullMessageFlux(): Subscription<string> {
  return client.subscribe('PushTypeEndpoint', 'getNonNullMessageFlux', {});
}

export function getSubscription(): Subscription<string | undefined> {
  return client.subscribe('PushTypeEndpoint', 'getSubscription', {});
}
