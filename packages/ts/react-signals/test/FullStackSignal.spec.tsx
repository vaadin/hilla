/* eslint-disable @typescript-eslint/unbound-method */
import { render, cleanup } from '@testing-library/react';
import { ActionOnLostSubscription, ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import { nanoid } from 'nanoid';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { afterEach, beforeEach, chai, describe, expect, it } from 'vitest';
import type {
  IncrementStateEvent,
  ReplaceStateEvent,
  SetStateEvent,
  SnapshotStateEvent,
  StateEvent,
} from '../src/events.js';
import { DependencyTrackingSignal } from '../src/FullStackSignal.js';
import { computed, NumberSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange } from './utils.js';

chai.use(sinonChai);
chai.use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  describe('DependencyTrackingSignal', () => {
    class TestSignal<T = unknown> extends DependencyTrackingSignal<T> {
      constructor(value: T | undefined, onSubscribe: () => void, onUnsubscribe: () => void) {
        super(value, onSubscribe, onUnsubscribe);
        this.subscribe(() => {}); // Ignores the internal subscription.
      }
    }

    let onFirstSubscribe: sinon.SinonStub;
    let onLastUnsubscribe: sinon.SinonStub;
    let signal: TestSignal;

    beforeEach(() => {
      onFirstSubscribe = sinon.stub();
      onLastUnsubscribe = sinon.stub();
      signal = new TestSignal(undefined, onFirstSubscribe, onLastUnsubscribe);
    });

    afterEach(() => {
      cleanup();
      sinon.resetHistory();
    });

    it('should call onSubscribe when the first subscription is created', () => {
      expect(onFirstSubscribe).not.to.have.been.called;
      signal.subscribe(() => {});
      expect(onFirstSubscribe).to.have.been.calledOnce;
    });

    it('should call onUnsubscribe when the last subscription is removed', () => {
      expect(onLastUnsubscribe).not.to.have.been.called;
      const subscriptionDisposeFnc = signal.subscribe(() => {});
      expect(onLastUnsubscribe).not.to.have.been.called;
      subscriptionDisposeFnc();
      expect(onLastUnsubscribe).to.have.been.calledOnce;
    });
  });

  describe('FullStackSignal', () => {
    function createAcceptedEvent(
      value: number,
      type: 'increment' | 'replace' | 'set' | 'snapshot',
    ): IncrementStateEvent | ReplaceStateEvent<number> | SetStateEvent<number> | SnapshotStateEvent<number> {
      return { id: nanoid(), type, value, expected: 0, accepted: true };
    }

    function simulateResubscription(
      connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<StateEvent>>,
      client: sinon.SinonStubbedInstance<ConnectClient>,
    ) {
      const [onSubscriptionLostCallback] = connectSubscriptionMock.onSubscriptionLost.firstCall.args;
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      if (onSubscriptionLostCallback() === ActionOnLostSubscription.RESUBSCRIBE) {
        client.subscribe('TestEndpoint', 'testMethod');
      }
    }

    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent>>;
    let signal: NumberSignal;

    beforeEach(() => {
      client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();

      subscription = createSubscriptionStub();
      // Mock the subscribe method
      client.subscribe.returns(subscription);

      signal = new NumberSignal(undefined, { client, endpoint: 'TestEndpoint', method: 'testMethod' });
      client.call.resetHistory();
    });

    afterEach(() => {
      sinon.resetHistory();
    });

    it('should create signal instance of type NumberSignal', () => {
      expect(signal).to.be.instanceOf(NumberSignal);
      expect(signal.value).to.be.undefined;
    });

    it('should not subscribe to signal provider endpoint before being subscribed to', () => {
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to signal provider endpoint only after being subscribed to', async () => {
      expect(client.subscribe).not.to.have.been.called;

      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
        parentClientSignalId: undefined,
      });
    });

    it('should not call client subscribe after being connected to the server instance', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
        parentClientSignalId: undefined,
      });

      const dependentSignal = computed(() => signal.value);
      expect(client.subscribe).to.be.have.been.calledOnce;

      render(<span>Value is {dependentSignal.value}</span>);
      await nextFrame();
      expect(client.subscribe).to.be.have.been.calledOnce;
    });

    it('should retain and send the params passed to the config at the time of creating the signal the server when subscribing', async () => {
      signal = new NumberSignal(undefined, {
        client,
        endpoint: 'TestEndpoint',
        method: 'testMethod',
        params: { foo: 'bar', baz: true },
      });
      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: { foo: 'bar', baz: true },
        parentClientSignalId: undefined,
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      signal.value = 42;

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.call).to.be.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: signal.id,
          event: { type: 'set', value: 42 },
        },
        { mute: true },
      );
    });

    it('should not subscribe on the fly when updating if already subscribed', () => {
      signal.subscribe(() => {});
      client.subscribe.resetHistory();
      signal.value = 42;
      expect(client.subscribe).not.to.have.been.called;
      expect(client.call).to.have.been.calledOnce;
    });

    it("should update signal's value based on the received event", async () => {
      expect(signal.value).to.be.undefined;

      render(<span>Value is {signal}</span>);
      await nextFrame();

      // Simulate the event received from the server:
      const snapshotEvent = createAcceptedEvent(42, 'snapshot');
      simulateReceivedChange(subscription, snapshotEvent);

      // Check if the signal value is updated:
      expect(signal.value).to.equal(42);
    });

    it('should render the updated value', async () => {
      const numberSignal = signal;

      let result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      simulateReceivedChange(subscription, createAcceptedEvent(42, 'snapshot'));

      result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');

      simulateReceivedChange(subscription, createAcceptedEvent(99, 'set'));
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 99');
    });

    it('should subscribe using client', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
        parentClientSignalId: undefined,
      });
    });

    it('should publish the new value to the server when set', () => {
      signal.value = 42;
      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          event: { type: 'set', value: 42 },
        },
        { mute: true },
      );

      signal.value = 0;

      client.call.resetHistory();

      signal.value += 1;
      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          event: { type: 'set', value: 1 },
        },
        { mute: true },
      );

      const [, , params] = client.call.firstCall.args;

      expect(params!.event).to.have.property('id');
    });

    it('should provide a way to access connection errors', async () => {
      const error = new Error('Server error');
      client.call.rejects(error);

      signal.value = 42;
      // Waiting for the ConnectionClient#call promise to resolve.
      await nextFrame();

      expect(signal.error).to.be.like({ value: error });

      // No error after the correct update
      client.call.resolves();
      signal.value = 50;
      await nextFrame();
      expect(signal.error).to.be.like({ value: undefined });
    });

    it('should provide a way to access the pending state', async () => {
      expect(signal.pending).to.be.like({ value: false });
      signal.value = 42;
      expect(signal.pending).to.be.like({ value: true });
      await nextFrame();
      expect(signal.pending).to.be.like({ value: false });
    });

    it('should provide an internal server subscription', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();
      expect(signal.server.subscription).to.equal(subscription);
    });

    it('should disconnect from the server', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();

      signal.server.disconnect();
      expect(subscription.cancel).to.have.been.calledOnce;
    });

    it('should throw an error when the server call fails', () => {
      client.call.rejects(new Error('Server error'));
      signal.value = 42;
    });

    it('should resubscribe when reconnecting', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();
      simulateResubscription(subscription, client);

      expect(client.subscribe).to.be.have.been.calledTwice;
    });

    it('should send undefined as parentClientSignalId when no parent signal is provided', async () => {
      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
        parentClientSignalId: undefined,
      });
    });

    it('should send parentClientSignalId when parent signal is provided', async () => {
      signal = new NumberSignal(undefined, {
        client,
        endpoint: 'TestEndpoint',
        method: 'testMethod',
        parentClientSignalId: '1234',
      });
      render(<span>Value is {signal}</span>);
      await nextFrame();

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
        parentClientSignalId: '1234',
      });
    });

    it('should not generate a random id when id is provided for the constructor', () => {
      signal = new NumberSignal(
        undefined,
        {
          client,
          endpoint: 'TestEndpoint',
          method: 'testMethod',
        },
        '1234',
      );
      expect(signal.id).to.equal('1234');
    });

    it('should send the provided id as event id when sending set events to the server', async () => {
      signal = new NumberSignal(
        undefined,
        {
          client,
          endpoint: 'TestEndpoint',
          method: 'testMethod',
          parentClientSignalId: 'a1b2c3d4',
        },
        '1234',
      );
      render(<span>Value is {signal}</span>);
      await nextFrame();

      signal.value = 42;
      expect(client.call).to.have.been.calledWith(
        'SignalsHandler',
        'update',
        {
          clientSignalId: '1234',
          event: { id: '1234', type: 'set', value: 42, accepted: false, parentSignalId: 'a1b2c3d4' },
        },
        { mute: true },
      );
    });
  });
});
