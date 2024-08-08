/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { NumberSignal, type SignalChannel } from '../src/index.js';
import { createSignalChannel, type StateChange, StateChangeType } from '../src/SignalChannel.js';
import { nextFrame } from './utils.js';

use(sinonChai);

describe('@vaadin/hilla-react-signals', () => {
  describe('SignalChannel', () => {
    function simulateReceivedChange(
      connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<StateChange<number>>>,
      change: StateChange<number>,
    ) {
      const onNextCallback = connectSubscriptionMock.onNext.getCall(0).args[0];
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      onNextCallback(change);
    }

    let connectClientMock: sinon.SinonStubbedInstance<ConnectClient>;
    let connectSubscriptionMock: sinon.SinonSpiedInstance<Subscription<StateChange<number>>>;
    let signal: NumberSignal;
    let channel: SignalChannel<NumberSignal>;

    beforeEach(() => {
      connectClientMock = sinon.createStubInstance(ConnectClient);
      connectClientMock.call.resolves();

      connectSubscriptionMock = sinon.spy<Subscription<StateChange<number>>>({
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
      connectClientMock.subscribe.returns(connectSubscriptionMock);

      signal = new NumberSignal();
      channel = createSignalChannel(signal, 'testEndpoint', connectClientMock);
      connectClientMock.call.resetHistory();
    });

    afterEach(() => {
      sinon.resetHistory();
    });

    it('should create signal instance of type NumberSignal', () => {
      expect(signal).to.be.instanceOf(NumberSignal);
      expect(signal.value).to.be.undefined;
    });

    it('should subscribe to signal provider endpoint', () => {
      expect(connectClientMock.subscribe).to.be.have.been.calledOnce;
      expect(connectClientMock.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: channel.id,
        signalProviderEndpointMethod: 'testEndpoint',
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      signal.value = 42;

      expect(connectClientMock.call).to.be.have.been.calledOnce;
      expect(connectClientMock.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: channel.id,
        change: { type: StateChangeType.SET, value: 42 },
      });
    });

    it("should update signal's value based on the received event", () => {
      expect(signal.value).to.be.undefined;

      // Simulate the event received from the server:
      const snapshotEvent: StateChange<number> = { id: 'someId', type: StateChangeType.SNAPSHOT, value: 42 };
      simulateReceivedChange(connectSubscriptionMock, snapshotEvent);

      // Check if the signal value is updated:
      expect(signal.value).to.equal(42);
    });

    it('should render the updated value', async () => {
      const numberSignal = signal;
      simulateReceivedChange(connectSubscriptionMock, { id: 'someId', type: StateChangeType.SNAPSHOT, value: 42 });

      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');

      simulateReceivedChange(connectSubscriptionMock, { id: 'someId', type: StateChangeType.SNAPSHOT, value: 99 });
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 99');
    });

    it('should subscribe using client', () => {
      expect(connectClientMock.subscribe).to.be.have.been.calledOnce;
      expect(connectClientMock.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        signalProviderEndpointMethod: 'testEndpoint',
        clientSignalId: channel.id,
      });
    });

    it('should publish the new value to the server when set', () => {
      signal.value = 42;
      expect(connectClientMock.call).to.have.been.calledOnce;
      expect(connectClientMock.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        change: { type: StateChangeType.SET, value: 42 },
      });

      signal.value = 0;

      connectClientMock.call.resetHistory();

      signal.value += 1;
      expect(connectClientMock.call).to.have.been.calledOnce;
      expect(connectClientMock.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        change: { type: StateChangeType.SET, value: 1 },
      });
    });

    it('should throw an error when the server call fails', async () => {
      connectClientMock.call.rejects(new Error('Server error'));
      signal.value = 42;
    });
  });
});
