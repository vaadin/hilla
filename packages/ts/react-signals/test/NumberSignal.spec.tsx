/* eslint-disable @typescript-eslint/unbound-method */

import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { beforeEach, describe, expect, it, chai } from 'vitest';
import type { SignalCommand } from '../src/commands.js';
import { createIncrementCommand, createSnapshotCommand } from '../src/commands.js';
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

  // Helper function to create commands for testing server responses
  function createServerCommand(commandId: string, type: 'increment' | 'snapshot', value: number): SignalCommand {
    const targetNodeId = '';

    if (type === 'increment') {
      const command = createIncrementCommand(targetNodeId, value);
      return { ...command, commandId };
    }

    const nodes = {
      [targetNodeId]: {
        '@type': 'NumberSignal',
        parent: null,
        lastUpdate: null,
        scopeOwner: null,
        value,
        listChildren: [],
        mapChildren: {},
      },
    };
    const command = createSnapshotCommand(nodes);
    return { ...command, commandId };
  }

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
      const expectedCommand1 = createServerCommand((params1!.event as { commandId: string }).commandId, 'increment', 1);
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          event: expectedCommand1,
        },
        { mute: true },
      );

      simulateReceivingAcceptedCommand(expectedCommand1);
      expect(numberSignal.value).to.equal(43);

      numberSignal.incrementBy(2);
      const [, , params2] = client.call.secondCall.args;
      const expectedCommand2 = createServerCommand((params2!.event as { commandId: string }).commandId, 'increment', 2);
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          event: expectedCommand2,
        },
        { mute: true },
      );

      simulateReceivingAcceptedCommand(expectedCommand2);
      expect(numberSignal.value).to.equal(45);

      numberSignal.incrementBy(-5);
      const [, , params3] = client.call.thirdCall.args;
      const expectedCommand3 = createServerCommand(
        (params3!.event as { commandId: string }).commandId,
        'increment',
        -5,
      );
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignal.id,
          event: expectedCommand3,
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

      const expectedCommand = createServerCommand('testId', 'increment', 1);
      simulateReceivingAcceptedCommand(expectedCommand);

      expect(numberSignal.value).to.equal(43);
    });

    it('should resolve the result promise after incrementBy', async () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      const { result } = numberSignal.incrementBy(1);
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(
        subscription,
        createServerCommand((params!.event as { commandId: string }).commandId, 'increment', 1),
      );
      await expect(result).to.be.fulfilled;
    });

    it('should resolve the result promise after incrementing by zero without server roundtrip', async () => {
      const numberSignal = new NumberSignal(42, config);
      subscribeToSignalViaEffect(numberSignal);
      await expect(numberSignal.incrementBy(0).result).to.be.fulfilled;
    });
  });
});
