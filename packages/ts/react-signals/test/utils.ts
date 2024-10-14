import type { Signal } from '@preact/signals-react';
import type { Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import type { StateEvent } from '../src/events.js';
import { effect } from '../src/index.js';

export async function nextFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      resolve();
    });
  });
}

export function createSubscriptionStub<T>(): sinon.SinonSpiedInstance<Subscription<StateEvent>> {
  return sinon.spy<Subscription<StateEvent>>({
    cancel() {},
    context() {
      return this;
    },
    onComplete() {
      return this;
    },
    onError() {
      return this;
    },
    onNext() {
      return this;
    },
    onSubscriptionLost() {
      return this;
    },
  });
}

export function subscribeToSignalViaEffect<T>(signal: Signal<T>): Array<T | undefined> {
  const results: Array<T | undefined> = [];
  effect(() => {
    results.push(signal.value);
  });
  return results;
}
