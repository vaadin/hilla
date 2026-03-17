/* eslint-disable @typescript-eslint/unbound-method */
import { render, cleanup } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { afterEach, beforeEach, describe, expect, it, chai } from 'vitest';
import type { SignalCommand, SetCommand } from '../src/commands.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { ValueSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange, subscribeToSignalViaEffect } from './utils.js';

chai.use(sinonChai);
chai.use(chaiLike);
chai.use(chaiAsPromised);

describe('@vaadin/hilla-react-signals', () => {
  type Person = {
    name: string;
    age: number;
    registered: boolean;
  };

  let config: ServerConnectionConfig;
  let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;

  beforeEach(() => {
    client = sinon.createStubInstance(ConnectClient);
    client.call.resolves();
    subscription = createSubscriptionStub();
    // Mock the subscribe method
    client.subscribe.returns(subscription);
    config = { client, endpoint: 'TestEndpoint', method: 'testMethod' };
  });

  afterEach(() => {
    cleanup();
  });

  describe('ValueSignal', () => {
    it('should retain default value as initialized', () => {
      const valueSignal1 = new ValueSignal<string>(undefined, config);
      expect(valueSignal1.value).to.be.undefined;

      const valueSignal2 = new ValueSignal<string>('foo', config);
      expect(valueSignal2.value).to.equal('foo');

      const valueSignal3 = new ValueSignal<boolean>(true, config);
      expect(valueSignal3.value).to.equal(true);

      const valueSignal4 = new ValueSignal<number>(42, config);
      expect(valueSignal4.value).to.equal(42);

      const valueSignal5 = new ValueSignal<Person>({ name: 'Alice', age: 42, registered: true }, config);
      expect(valueSignal5.value).to.deep.equal({ name: 'Alice', age: 42, registered: true });
    });

    it('should render value when signal is rendered', async () => {
      const valueSignal = new ValueSignal<string>('foo', config);
      const result = render(<div>{valueSignal}</div>);
      await nextFrame();
      expect(result.container.textContent).to.equal('foo');
    });

    it('should set the value locally when calling set method without waiting for the server update', () => {
      const valueSignal = new ValueSignal<string>('foo', config);
      expect(valueSignal.value).to.equal('foo');
      valueSignal.set('bar');
      expect(valueSignal.value).to.equal('bar');
    });

    it('should be possible to subscribe to the value changes in effects', () => {
      const valueSignal = new ValueSignal<string>('foo', config);
      expect(valueSignal.value).to.equal('foo');

      const results = subscribeToSignalViaEffect(valueSignal);

      valueSignal.value = 'bar';
      valueSignal.value += 'baz';
      valueSignal.set('qux');
      expect(results).to.deep.equal(['foo', 'bar', 'barbaz', 'qux']);
    });

    it('should not subscribe to signal provider endpoint before being subscribed to', () => {
      const _ = new ValueSignal<string>(undefined, config);
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to signal provider endpoint only after being subscribed to', () => {
      const valueSignal = new ValueSignal<string>(undefined, config);
      expect(client.subscribe).not.to.have.been.called;

      subscribeToSignalViaEffect(valueSignal);

      expect(client.subscribe).to.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: valueSignal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
        params: undefined,
      });
    });

    it('should resolve the result promise after set', async () => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      const { result } = valueSignal.set('b');
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        commandId: (params!.command as { commandId: string }).commandId,
        targetNodeId: '',
        '@type': 'set',
        value: 'b',
      } as SetCommand<string>);
      await expect(result).to.be.fulfilled;
    });

    it('should skip re-applying own confirmed set', () => {
      const valueSignal = new ValueSignal<string>('original', config);
      subscribeToSignalViaEffect(valueSignal);

      valueSignal.set('updated');
      expect(valueSignal.value).to.equal('updated');

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      // Server confirms — value should remain 'updated', not re-apply
      simulateReceivedChange(subscription, {
        commandId,
        targetNodeId: '',
        '@type': 'set',
        value: 'updated',
      } as SetCommand<string>);

      expect(valueSignal.value).to.equal('updated');
    });

    it('should not overwrite newer optimistic set when earlier set is confirmed', () => {
      const valueSignal = new ValueSignal<string>('original', config);
      subscribeToSignalViaEffect(valueSignal);

      valueSignal.set('first');
      const [, , params1] = client.call.firstCall.args;
      const firstCommandId = (params1!.command as { commandId: string }).commandId;

      valueSignal.set('second');
      expect(valueSignal.value).to.equal('second');

      // Server confirms the first set — should not overwrite 'second'
      simulateReceivedChange(subscription, {
        commandId: firstCommandId,
        targetNodeId: '',
        '@type': 'set',
        value: 'first',
      } as SetCommand<string>);

      expect(valueSignal.value).to.equal('second');
    });

    it('should revert to confirmed value on set rejection', () => {
      const valueSignal = new ValueSignal<string>('original', config);
      subscribeToSignalViaEffect(valueSignal);

      const { result } = valueSignal.set('rejected-value');
      result.catch(() => {});
      expect(valueSignal.value).to.equal('rejected-value');

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      simulateReceivedChange(subscription, {
        commandId,
        targetNodeId: '',
        '@type': 'set',
        value: 'rejected-value',
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      // Should revert to the confirmed value
      expect(valueSignal.value).to.equal('original');
    });

    it('should update confirmed value from snapshot', () => {
      const valueSignal = new ValueSignal<string>('original', config);
      subscribeToSignalViaEffect(valueSignal);

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
            value: 'from-snapshot',
            listChildren: [],
            mapChildren: {},
          },
        },
      } as unknown as SignalCommand);

      expect(valueSignal.value).to.equal('from-snapshot');
    });
  });
});
