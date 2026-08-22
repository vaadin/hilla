/* eslint-disable @typescript-eslint/unbound-method */
import { render, cleanup } from '@testing-library/react';
import { ActionOnLostSubscription, ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { afterEach, beforeEach, chai, describe, expect, it } from 'vitest';
import type { SignalCommand } from '../src/commands.js';
import { createSetCommand, createSnapshotCommand } from '../src/commands.js';
import { NumberSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange, subscribeToSignalViaEffect } from './utils.js';

chai.use(sinonChai);
chai.use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  describe('FullStackSignal', () => {
    function simulateResubscription(
      connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<SignalCommand>>,
      client: sinon.SinonStubbedInstance<ConnectClient>,
    ) {
      const [onSubscriptionLostCallback] = connectSubscriptionMock.onSubscriptionLost.firstCall.args;
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      if (onSubscriptionLostCallback() === ActionOnLostSubscription.RESUBSCRIBE) {
        client.subscribe('TestEndpoint', 'testMethod');
      }
    }

    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
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
      cleanup();
      sinon.resetHistory();
    });

    it('should create signal instance of type NumberSignal', () => {
      expect(signal).to.be.instanceOf(NumberSignal);
      expect(signal.value).to.be.undefined;
    });

    it('should not subscribe to signal provider endpoint before being subscribed to', () => {
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to signal provider endpoint only after being subscribed to', () => {
      expect(client.subscribe).not.to.have.been.called;

      subscribeToSignalViaEffect(signal);

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
      });
    });

    it('should retain and send the params passed to the config at the time of creating the signal', () => {
      signal = new NumberSignal(undefined, {
        client,
        endpoint: 'TestEndpoint',
        method: 'testMethod',
        params: { foo: 'bar', baz: true },
      });
      subscribeToSignalViaEffect(signal);

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: { foo: 'bar', baz: true },
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      subscribeToSignalViaEffect(signal);

      signal.value = 42;

      expect(client.call).to.be.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: signal.id,
          command: { '@type': 'set', value: 42 },
        },
        { mute: true },
      );
    });

    it('should not subscribe on the fly when updating if already subscribed', () => {
      subscribeToSignalViaEffect(signal);
      client.subscribe.resetHistory();
      signal.value = 42;
      expect(client.subscribe).not.to.have.been.called;
      expect(client.call).to.have.been.calledOnce;
    });

    it("should update signal's value based on the received event", () => {
      expect(signal.value).to.be.undefined;

      subscribeToSignalViaEffect(signal);

      // Simulate the command received from the server:
      const nodes = {
        '': {
          '@type': 'ValueSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          value: 42,
          listChildren: [],
          mapChildren: {},
        },
      };
      const snapshotCommand = createSnapshotCommand(nodes);
      simulateReceivedChange(subscription, snapshotCommand);

      // Check if the signal value is updated:
      expect(signal.value).to.equal(42);
    });

    it('should render the updated value', async () => {
      const numberSignal = signal;

      subscribeToSignalViaEffect(numberSignal);

      const nodes = {
        '': {
          '@type': 'ValueSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          value: 42,
          listChildren: [],
          mapChildren: {},
        },
      };
      simulateReceivedChange(subscription, createSnapshotCommand(nodes));

      const result = render(<span>Value is {numberSignal.value}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');

      simulateReceivedChange(subscription, createSetCommand('', 99));
      const result2 = render(<span>Value is {numberSignal.value}</span>);
      await nextFrame();
      expect(result2.container.textContent).to.equal('Value is 99');
    });

    it('should subscribe using client', () => {
      subscribeToSignalViaEffect(signal);

      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
      });
    });

    it('should publish the new value to the server when set', () => {
      subscribeToSignalViaEffect(signal);

      signal.value = 42;
      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          command: { '@type': 'set', value: 42 },
        },
        { mute: true },
      );

      signal.value = 1;
      expect(client.call).to.have.been.calledTwice;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          command: { '@type': 'set', value: 1 },
        },
        { mute: true },
      );

      const [, , params] = client.call.firstCall.args;
      expect(params!.command).to.have.property('commandId');
    });

    it('should provide a way to access connection errors', async () => {
      const error = new Error('Server error');
      client.call.rejects(error);

      subscribeToSignalViaEffect(signal);
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
      subscribeToSignalViaEffect(signal);

      expect(signal.pending).to.be.like({ value: false });
      signal.value = 42;
      expect(signal.pending).to.be.like({ value: true });
      await nextFrame();
      expect(signal.pending).to.be.like({ value: false });
    });

    it('should provide an internal server subscription', () => {
      subscribeToSignalViaEffect(signal);
      expect(signal.connection.subscription).to.equal(subscription);
    });

    it('should disconnect from the server', () => {
      subscribeToSignalViaEffect(signal);
      signal.connection.disconnect();
      expect(subscription.cancel).to.have.been.calledOnce;
    });

    it('should throw an error when the server call fails', () => {
      client.call.rejects(new Error('Server error'));
      subscribeToSignalViaEffect(signal);
      signal.value = 42;
    });

    it('should resubscribe when reconnecting', () => {
      subscribeToSignalViaEffect(signal);
      simulateResubscription(subscription, client);

      expect(client.subscribe).to.be.have.been.calledTwice;
    });

    it('should fall back to confirmed tree when unconfirmed command fails to apply', () => {
      const numberSignal = new NumberSignal(undefined, { client, endpoint: 'TestEndpoint', method: 'testMethod' });
      subscribeToSignalViaEffect(numberSignal);

      // Receive a snapshot setting value to a string (non-numeric)
      const nodes = {
        '': {
          '@type': 'ValueSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          value: 'not-a-number',
          listChildren: [],
          mapChildren: {},
        },
      };
      simulateReceivedChange(subscription, createSnapshotCommand(nodes));

      // Now try to increment — will fail in applyCommand, should fall back to confirmed tree
      numberSignal.incrementBy(5);
      expect(numberSignal.value).to.equal('not-a-number');
    });

    it('should support valueOf, toString, and toJSON', () => {
      const numberSignal = new NumberSignal(42, { client, endpoint: 'TestEndpoint', method: 'testMethod' });
      expect(numberSignal.valueOf()).to.equal(42);
      expect(numberSignal.toString()).to.equal('42');
      expect(numberSignal.toJSON()).to.equal(42);
    });

    it('should not throw when disconnecting without active subscription', () => {
      // signal has not been subscribed to yet
      expect(() => signal.connection.disconnect()).not.to.throw();
    });

    it('should wrap non-Error rejection as Error', async () => {
      // eslint-disable-next-line @typescript-eslint/prefer-promise-reject-errors
      client.call.returns(Promise.reject(42));

      subscribeToSignalViaEffect(signal);
      signal.value = 42;
      await nextFrame();

      expect(signal.error.value).to.be.instanceOf(Error);
      expect(signal.error.value?.message).to.equal('42');
    });

    it('should peek at the value without subscribing', () => {
      const numberSignal = new NumberSignal(42, { client, endpoint: 'TestEndpoint', method: 'testMethod' });
      expect(numberSignal.peek()).to.equal(42);
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
  });
});
