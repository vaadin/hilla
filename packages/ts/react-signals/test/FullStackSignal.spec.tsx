/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { type StateEvent, StateEventType } from '../src/FullStackSignal.js';
import { NumberSignal } from '../src/index.js';
import { nextFrame } from './utils.js';

use(sinonChai);

describe('@vaadin/hilla-react-signals', () => {
  describe('FullStackSignal', () => {
    function simulateReceivedChange(
      connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<StateEvent<number>>>,
      event: StateEvent<number>,
    ) {
      const [onNextCallback] = connectSubscriptionMock.onNext.firstCall.args;
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      onNextCallback(event);
    }

    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent<number>>>;
    let signal: NumberSignal;

    beforeEach(() => {
      client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();

      subscription = sinon.spy<Subscription<StateEvent<number>>>({
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

    it('should subscribe to signal provider endpoint', () => {
      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      signal.value = 42;

      expect(client.call).to.be.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: signal.id,
        event: { type: StateEventType.SET, value: 42 },
      });
    });

    it("should update signal's value based on the received event", () => {
      expect(signal.value).to.be.undefined;

      // Simulate the event received from the server:
      const snapshotEvent: StateEvent<number> = { id: 'someId', type: StateEventType.SNAPSHOT, value: 42 };
      simulateReceivedChange(subscription, snapshotEvent);

      // Check if the signal value is updated:
      expect(signal.value).to.equal(42);
    });

    it('should render the updated value', async () => {
      const numberSignal = signal;
      simulateReceivedChange(subscription, { id: 'someId', type: StateEventType.SNAPSHOT, value: 42 });

      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');

      simulateReceivedChange(subscription, { id: 'someId', type: StateEventType.SNAPSHOT, value: 99 });
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 99');
    });

    it('should subscribe using client', () => {
      expect(client.subscribe).to.be.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: signal.id,
        providerEndpoint: 'TestEndpoint',
        providerMethod: 'testMethod',
      });
    });

    it('should publish the new value to the server when set', () => {
      signal.value = 42;
      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        event: { type: StateEventType.SET, value: 42 },
      });

      signal.value = 0;

      client.call.resetHistory();

      signal.value += 1;
      expect(client.call).to.have.been.calledOnce;
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        event: { type: StateEventType.SET, value: 1 },
      });
    });

    it('should throw an error when the server call fails', () => {
      client.call.rejects(new Error('Server error'));
      signal.value = 42;
    });
  });
});
