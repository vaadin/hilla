/* eslint-disable @typescript-eslint/unbound-method */
// eslint-disable-next-line import/no-unassigned-import
import './setup.js';

import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import { expect, use } from 'chai';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { ReplaceStateEvent, StateEvent } from '../src/events.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { ValueSignal } from '../src/index.js';
import { createSubscriptionStub, nextFrame, simulateReceivedChange, subscribeToSignalViaEffect } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  type Person = {
    name: string;
    age: number;
    registered: boolean;
  };

  let config: ServerConnectionConfig;
  let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;

  beforeEach(() => {
    client = sinon.createStubInstance(ConnectClient);
    client.call.resolves();
    subscription = createSubscriptionStub();
    // Mock the subscribe method
    client.subscribe.returns(subscription);
    config = { client, endpoint: 'TestEndpoint', method: 'testMethod' };
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
        parentClientSignalId: undefined,
      });
    });

    it('should send correct event and set the value when receiving snapshot event after calling replace', () => {
      const valueSignal = new ValueSignal<string>('bar', config);
      subscribeToSignalViaEffect(valueSignal);

      valueSignal.replace('bar', 'foo');

      const [, , params] = client.call.firstCall.args;

      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: valueSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params?.event.id, type: 'replace', value: 'foo', expected: 'bar' },
      });

      const [onNextCallback] = subscription.onNext.firstCall.args;
      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params?.event.id, type: 'snapshot', value: 'bar' });
      // verify receiving the snapshot event updates the value correctly:
      expect(valueSignal.value).to.equal('bar');
    });

    it('should not set the value when receiving reject event after calling replace', () => {
      const valueSignal = new ValueSignal<string>('baz', config);
      subscribeToSignalViaEffect(valueSignal);

      valueSignal.replace('bar', 'barfoo');

      const [, , params] = client.call.firstCall.args;
      const [onNextCallback] = subscription.onNext.firstCall.args;
      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params?.event.id, type: 'reject', value: 'dont care' });
      // verify receiving the reject event doesn't change the value:
      expect(valueSignal.value).to.equal('baz');
    });

    it('should resolve the result promise after set', (done) => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      valueSignal.set('b').result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'set',
        value: 'b',
        accepted: true,
      });
    });

    it('should reject the result promise after rejected set', (done) => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      valueSignal.set('b').result.then(
        () => done('Should not resolve'),
        () => done(),
      );
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'set',
        value: 'b',
        accepted: false,
      });
      setTimeout(done, 100);
    });

    it('should resolve the result promise after replace', (done) => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      valueSignal.replace('a', 'b').result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'replace',
        value: 'b',
        accepted: true,
      });
    });

    it('should reject the result promise after rejected replace', (done) => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      valueSignal.replace('a', 'b').result.then(
        () => done('Should not resolve'),
        () => done(),
      );
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'replace',
        value: 'b',
        accepted: false,
      });
      setTimeout(done, 100);
    });

    it('should resolve the result promise after update', (done) => {
      const valueSignal = new ValueSignal<string>('a', config);
      subscribeToSignalViaEffect(valueSignal);
      valueSignal.update(() => 'b').result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      simulateReceivedChange(subscription, {
        id: (params!.event as { id: string }).id,
        type: 'set',
        value: 'b',
        accepted: true,
      });
    });

    it('should send the correct event and update the value when receiving accepted event after calling update', async () => {
      const valueSignal = new ValueSignal<string>('ba', config);
      render(<div>{valueSignal}</div>);
      await nextFrame();

      valueSignal.update((currValue) => `${currValue}r`);
      const [, , params] = client.call.firstCall.args;

      expect(client.call).to.have.been.calledOnce;
      // @ts-expect-error params.event type has id property
      const expectedEvent = { id: params?.event.id, type: 'replace', value: 'bar', expected: 'ba' };
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: valueSignal.id,
        event: expectedEvent,
      });

      const [onNextCallback] = subscription.onNext.firstCall.args;
      onNextCallback({ ...expectedEvent, accepted: true });
      expect(valueSignal.value).to.equal('bar');
    });

    it('should send correct subsequent events and not update the value when receiving reject event after calling update', async () => {
      const valueSignal = new ValueSignal<string>('a', config);
      expect(valueSignal.value).to.equal('a');
      render(<div>{valueSignal}</div>);
      await nextFrame();

      const updateOperation = valueSignal.update((currValue) => `${currValue}a`);
      expect(client.call).to.have.been.calledOnce;
      const [, , params1] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: valueSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params1?.event.id, type: 'replace', value: 'aa', expected: 'a' },
      });

      const [onNextCallback] = subscription.onNext.firstCall.args;

      // Simulate an accepted event representing a concurrent value change before the reject is received:
      onNextCallback({
        id: 'another-event-id',
        type: 'replace',
        value: 'b',
        expected: 'a',
        accepted: true,
      } as ReplaceStateEvent<string>);
      expect(valueSignal.value).to.equal('b');

      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params1?.event.id, type: 'replace', value: 'aa', expected: 'a', accepted: false });
      // verify that the value is not updated after receiving a reject event:
      expect(valueSignal.value).to.equal('b');
      // verify that receiving reject event triggers another update call:
      expect(client.call).to.have.been.calledTwice;
      const [, , params2] = client.call.secondCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: valueSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params2?.event.id, type: 'replace', value: 'ba', expected: 'b', accepted: false },
      });

      // Simulate another concurrent value change before the reject is received:
      onNextCallback({
        id: 'another-event-id',
        type: 'replace',
        value: 'c',
        expected: 'b',
        accepted: true,
      } as ReplaceStateEvent<string>);
      expect(valueSignal.value).to.equal('c');

      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params2?.event.id, type: 'replace', value: 'ba', expected: 'b', accepted: false });
      expect(client.call).to.have.been.calledThrice;
      const [, , params3] = client.call.thirdCall.args;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: valueSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params3?.event.id, type: 'replace', value: 'ca', expected: 'c', accepted: false },
      });

      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params3?.event.id, type: 'replace', value: 'ca', expected: 'c', accepted: false });
      expect(client.call).to.have.been.callCount(4);

      setTimeout(() => updateOperation.cancel(), 500);
      expect(valueSignal.value).to.equal('c');
    });

    it('should update the value when receive accepted event following rejected events after calling update', async () => {
      const valueSignal = new ValueSignal<string>('foo', config);
      expect(valueSignal.value).to.equal('foo');
      render(<div>{valueSignal}</div>);
      await nextFrame();
      const [onNextCallback] = subscription.onNext.firstCall.args;
      valueSignal.update((currValue) => `${currValue}bar`);
      const [, , params1] = client.call.firstCall.args;

      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params1?.event.id, type: 'replace', value: 'dont care', accepted: false });
      expect(valueSignal.value).to.equal('foo');

      const [, , params2] = client.call.secondCall.args;
      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params2?.event.id, type: 'replace', value: 'dont care', accepted: false });
      expect(valueSignal.value).to.equal('foo');

      const [, , params3] = client.call.thirdCall.args;
      // @ts-expect-error params.event type has id property
      onNextCallback({ id: params3?.event.id, type: 'replace', value: 'foobar', expected: 'foo', accepted: true });
      expect(valueSignal.value).to.equal('foobar');
    });
  });
});
