/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { StateEvent } from '../src/events.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { effect } from '../src/index.js';
import { NumberSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, subscribeToSignalViaEffect } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  let config: ServerConnectionConfig;
  let subscription: sinon.SinonStubbedInstance<Subscription<StateEvent<number>>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;

  function simulateReceivingSnapshot(eventId: string, value: number): void {
    const [onNextCallback] = subscription.onNext.firstCall.args;
    onNextCallback({ id: eventId, type: 'snapshot', value });
  }

  function simulateReceivingReject(eventId: string): void {
    const [onNextCallback] = subscription.onNext.firstCall.args;
    // @ts-expect-error value is not used in reject events
    onNextCallback({ id: eventId, type: 'reject', value: 0 });
  }

  beforeEach(() => {
    client = sinon.createStubInstance(ConnectClient);
    client.call.resolves();
    // Mock the subscribe method
    subscription = createSubscriptionStub();
    client.subscribe.returns(subscription);
    config = { client, endpoint: 'TestEndpoint', method: 'testMethod' };
  });

  describe('NumberSignal', () => {
    it('should retain default value as initialized', () => {
      const numberSignal1 = new NumberSignal(undefined, config);
      expect(numberSignal1.value).to.be.undefined;

      const numberSignal2 = new NumberSignal(0, config);
      expect(numberSignal2.value).to.equal(0);

      const numberSignal3 = new NumberSignal(42.424242, config);
      expect(numberSignal3.value).to.equal(42.424242);

      const numberSignal4 = new NumberSignal(-42.424242, config);
      expect(numberSignal4.value).to.equal(-42.424242);
    });

    it('should render value when signal is rendered', async () => {
      const numberSignal = new NumberSignal(42, config);
      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');
    });

    it('should set the underlying value locally without waiting for server confirmation', () => {
      const numberSignal = new NumberSignal(undefined, config);
      expect(numberSignal.value).to.be.undefined;
      numberSignal.value = 42;
      expect(numberSignal.value).to.equal(42);

      const anotherNumberSignal = new NumberSignal(undefined, config);

      const results: Array<number | undefined> = [];
      effect(() => {
        results.push(anotherNumberSignal.value);
      });
      anotherNumberSignal.value = 42;
      anotherNumberSignal.value += 1;

      expect(results).to.be.like([undefined, 42, 43]);
    });

    it('should send the correct values in replace events when increment is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.increment();
      const [, , params1] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params1?.event.id, type: 'replace', value: 43, expected: 42 },
      });
      // @ts-expect-error params.event type has id property
      simulateReceivingSnapshot(params1?.event.id, 43);
      expect(numberSignal.value).to.equal(43);

      numberSignal.increment(2);
      const [, , params2] = client.call.secondCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params2?.event.id, type: 'replace', value: 45, expected: 43 },
      });
      // @ts-expect-error params.event type has id property
      simulateReceivingSnapshot(params2?.event.id, 45);
      expect(numberSignal.value).to.equal(45);

      numberSignal.increment(-5);
      const [, , params3] = client.call.thirdCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params3?.event.id, type: 'replace', value: 40, expected: 45 },
      });
      // @ts-expect-error params.event type has id property
      simulateReceivingSnapshot(params3?.event.id, 40);
      expect(numberSignal.value).to.equal(40);
    });

    it('should retry after receiving reject events when increment is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      const incrementSubscription = numberSignal.increment();
      const [, , params1] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params1?.event.id, type: 'replace', value: 43, expected: 42 },
      });

      // @ts-expect-error params.event type has id property
      simulateReceivingReject(params1?.event.id);
      const [, , params2] = client.call.secondCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params2?.event.id, type: 'replace', value: 43, expected: 42 },
      });

      setTimeout(() => incrementSubscription.cancel(), 500);
    });

    it('should send the correct values in replace events when add is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.add(5);
      const [, , params1] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params1?.event.id, type: 'replace', value: 47, expected: 42 },
      });
      // @ts-expect-error params.event type has id property
      simulateReceivingSnapshot(params1?.event.id, 47);
      expect(numberSignal.value).to.equal(47);

      numberSignal.increment(-10);
      const [, , params2] = client.call.secondCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params2?.event.id, type: 'replace', value: 37, expected: 47 },
      });
      // @ts-expect-error params.event type has id property
      simulateReceivingSnapshot(params2?.event.id, 37);
      expect(numberSignal.value).to.equal(37);
    });

    it('should retry after receiving reject events when add is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      const incrementSubscription = numberSignal.add(-2, true);
      const [, , params1] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params1?.event.id, type: 'replace', value: 40, expected: 42 },
      });

      // @ts-expect-error params.event type has id property
      simulateReceivingReject(params1?.event.id);
      const [, , params2] = client.call.secondCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params2?.event.id, type: 'replace', value: 40, expected: 42 },
      });

      setTimeout(() => incrementSubscription.cancel(), 500);
    });
  });
});
