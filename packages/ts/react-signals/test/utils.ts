import type { Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import type { StateEvent } from '../src/events.js';

export async function nextFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      resolve();
    });
  });
}

export function createSubscriptionStub<T>(): sinon.SinonStubbedInstance<Subscription<StateEvent<T>>> {
  return sinon.stub<Subscription<StateEvent<T>>>({
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
  });
}
