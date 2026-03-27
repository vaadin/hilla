/* eslint-disable @typescript-eslint/unbound-method */

import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { beforeEach, describe, expect, it, chai } from 'vitest';
import type { SignalCommand, IncrementCommand } from '../src/commands.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { effect, NumberSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange, subscribeToSignalViaEffect } from './utils.js';

chai.use(sinonChai);
chai.use(chaiLike);
chai.use(chaiAsPromised);

describe('@vaadin/hilla-react-signals', () => {
  let config: ServerConnectionConfig;
  let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;

  function simulateReceivingAcceptedCommand(command: SignalCommand): void {
    const [onNextCallback] = subscription.onNext.firstCall.args;
    onNextCallback(command);
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
      const result = render(<span>Value is {numberSignal.value}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');
    });

    it('should set the underlying value locally without waiting for server confirmation', () => {
      const numberSignal = new NumberSignal(undefined, config);
      subscribeToSignalViaEffect(numberSignal);
      expect(numberSignal.value).to.be.undefined;
      numberSignal.value = 42;
      expect(numberSignal.value).to.equal(42);

      const anotherNumberSignal = new NumberSignal(undefined, config);
      subscribeToSignalViaEffect(anotherNumberSignal);

      const results: Array<number | undefined> = [];
      effect(() => {
        results.push(anotherNumberSignal.value);
      });
      anotherNumberSignal.value = 42;
      anotherNumberSignal.value = 43;

      expect(results).to.be.like([undefined, 42, 43]);
    });

    it('should send the correct values in increment events when incrementBy is called', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(1);
      const [, , params1] = client.call.firstCall.args;
      const expectedCommand1: IncrementCommand = {
        commandId: (params1!.command as { commandId: string }).commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: 1,
      };
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          command: expectedCommand1,
        },
        { mute: true },
      );

      simulateReceivingAcceptedCommand(expectedCommand1);
      expect(numberSignal.value).to.equal(43);

      numberSignal.incrementBy(2);
      const [, , params2] = client.call.secondCall.args;
      const expectedCommand2: IncrementCommand = {
        commandId: (params2!.command as { commandId: string }).commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: 2,
      };
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          command: expectedCommand2,
        },
        { mute: true },
      );

      simulateReceivingAcceptedCommand(expectedCommand2);
      expect(numberSignal.value).to.equal(45);

      numberSignal.incrementBy(-5);
      const [, , params3] = client.call.thirdCall.args;
      const expectedCommand3: IncrementCommand = {
        commandId: (params3!.command as { commandId: string }).commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: -5,
      };
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          command: expectedCommand3,
        },
        { mute: true },
      );

      simulateReceivingAcceptedCommand(expectedCommand3);
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

      const expectedCommand: IncrementCommand = {
        commandId: 'testId',
        targetNodeId: '',
        '@type': 'inc',
        delta: 1,
      };
      simulateReceivingAcceptedCommand(expectedCommand);

      expect(numberSignal.value).to.equal(43);
    });

    it('should resolve the result promise after incrementBy', async () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      const { result } = numberSignal.incrementBy(1);
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        commandId: (params!.command as { commandId: string }).commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: 1,
      } as IncrementCommand);
      await expect(result).to.be.fulfilled;
    });

    it('should resolve the result promise after incrementing by zero without server roundtrip', async () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      await expect(numberSignal.incrementBy(0).result).to.be.fulfilled;
    });

    it('should apply optimistic increment immediately', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(10);
      // Value should update before server confirms
      expect(numberSignal.value).to.equal(52);
    });

    it('should skip re-applying own confirmed increment', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(10);
      expect(numberSignal.value).to.equal(52);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      // Server confirms our increment — value should not change again
      simulateReceivedChange(subscription, {
        commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: 10,
      } as IncrementCommand);

      expect(numberSignal.value).to.equal(52);
    });

    it('should revert optimistic increment on rejection', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      const { result } = numberSignal.incrementBy(10);
      result.catch(() => {});
      expect(numberSignal.value).to.equal(52);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      simulateReceivedChange(subscription, {
        commandId,
        targetNodeId: '',
        '@type': 'inc',
        delta: 10,
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      expect(numberSignal.value).to.equal(42);
    });

    it('should return the integer part of the value from valueAsInt()', () => {
      const numberSignal = new NumberSignal(42.7, config);
      expect(numberSignal.valueAsInt()).to.equal(42);

      const negativeSignal = new NumberSignal(-3.9, config);
      expect(negativeSignal.valueAsInt()).to.equal(-3);
    });

    it('should clear pending increments on snapshot', () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);

      numberSignal.incrementBy(10);
      expect(numberSignal.value).to.equal(52);

      // Snapshot resets everything
      simulateReceivedChange(subscription, {
        commandId: 'snapshot-id',
        targetNodeId: '',
        '@type': 'snapshot',
        nodes: {
          '': {
            '@type': 'ValueSignal',
            parent: null,
            lastUpdate: null,
            scopeOwner: null,
            value: 100,
            listChildren: [],
            mapChildren: {},
          },
        },
      } as unknown as SignalCommand);

      expect(numberSignal.value).to.equal(100);
    });
  });
});
