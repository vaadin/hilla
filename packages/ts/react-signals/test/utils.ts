import type { Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import type { StateEvent } from '../src/events.js';
import { effect, type ValueSignal } from '../src/index.js';

export async function nextFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      resolve();
    });
  });
}

export function createSubscriptionStub<T>(): sinon.SinonSpiedInstance<Subscription<StateEvent<T>>> {
  return sinon.spy<Subscription<StateEvent<T>>>({
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
    onConnectionStateChange() {
      return this;
    },
  });
}

export function subscribeToSignalViaEffect<T>(signal: ValueSignal<T>): Array<T | undefined> {
  const results: Array<T | undefined> = [];
  effect(() => {
    results.push(signal.value);
  });
  return results;
}
