import type { Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import type { SignalCommand } from '../src/commands.js';
import { effect } from '../src/index.js';

export async function nextFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      resolve();
    });
  });
}

export function createSubscriptionStub(): sinon.SinonSpiedInstance<Subscription<SignalCommand>> {
  return sinon.spy<Subscription<SignalCommand>>({
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

export function simulateReceivedChange(
  connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<SignalCommand>>,
  command: SignalCommand,
): void {
  const [onNextCallback] = connectSubscriptionMock.onNext.firstCall.args;
  onNextCallback(command);
}

/**
 * Subscribes to a FullStackSignal or Signal via effect, collecting values.
 * Works with both `Signal<T>` and `FullStackSignal<T>` since both have `.value`.
 */
export function subscribeToSignalViaEffect<T>(signal: { readonly value: T }): Array<T | undefined> {
  const results: Array<T | undefined> = [];
  effect(() => {
    results.push(signal.value);
  });
  return results;
}
