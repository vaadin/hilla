/* eslint-disable @typescript-eslint/unbound-method */
// eslint-disable-next-line import/no-unassigned-import
import './setup.js';

import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { IncrementStateEvent, StateEvent } from '../src/events.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { effect, NumberSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange, subscribeToSignalViaEffect } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  let config: ServerConnectionConfig;
  let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;

  function simulateReceivingAcceptedEvent(event: StateEvent): void {
    const [onNextCallback] = subscription.onNext.firstCall.args;
    onNextCallback({ ...event, accepted: true });
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

    it('should send the correct values in increment events when incrementBy is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(1);
      const [, , params1] = client.call.firstCall.args;
      const expectedEvent1: IncrementStateEvent = {
        // @ts-expect-error params.event type has id property
        id: params1?.event.id,
        type: 'increment',
        value: 1,
        accepted: false,
      };
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        event: expectedEvent1,
      });

      simulateReceivingAcceptedEvent(expectedEvent1);
      expect(numberSignal.value).to.equal(43);

      numberSignal.incrementBy(2);
      const [, , params2] = client.call.secondCall.args;
      // @ts-expect-error params.event type has id property
      const expectedEvent2: IncrementStateEvent = { id: params2?.event.id, type: 'increment', value: 2 };
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        event: expectedEvent2,
      });

      simulateReceivingAcceptedEvent(expectedEvent2);
      expect(numberSignal.value).to.equal(45);

      numberSignal.incrementBy(-5);
      const [, , params3] = client.call.thirdCall.args;
      const expectedEvent3: IncrementStateEvent = {
        // @ts-expect-error params.event type has id property
        id: params3?.event.id,
        type: 'increment',
        value: -5,
        accepted: false,
      };
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: numberSignal.id,
        event: expectedEvent3,
      });

      simulateReceivingAcceptedEvent(expectedEvent3);
      expect(numberSignal.value).to.equal(40);
    });

    it('should update the underlying value locally without waiting for server confirmation when incrementBy is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(1);
      expect(numberSignal.value).to.equal(43);

      numberSignal.incrementBy(2);
      expect(numberSignal.value).to.equal(45);

      numberSignal.incrementBy(-5);
      expect(numberSignal.value).to.equal(40);
    });

    it('should not send any event to the server when incrementBy is called with zero as delta', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(0);
      expect(client.call).not.to.have.been.called;
    });

    it('should only apply the change to the value upon receiving accepted event that is not initiated by the NumberSignal itself', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      const expectedEvent: IncrementStateEvent = { id: 'testId', type: 'increment', value: 1, accepted: true };
      simulateReceivingAcceptedEvent(expectedEvent);

      expect(numberSignal.value).to.equal(43);
    });

    it('should resolve the result promise after incrementBy', (done) => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      numberSignal.incrementBy(1).result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'increment',
        value: 43,
        accepted: true,
      });
    });

    it('should reject the result promise after rejected incrementBy', (done) => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      numberSignal.incrementBy(1).result.then(
        () => done('Should not resolve'),
        () => done(),
      );
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'increment',
        value: 43,
        accepted: false,
      });
    });

    it('should resolve the result promise after incrementing by zero without server roundtrip', (done) => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      numberSignal.incrementBy(0).result.then(done, () => done('Should not reject'));
    });
  });
});
