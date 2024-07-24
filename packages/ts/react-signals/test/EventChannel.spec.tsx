/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { NumberSignal, NumberSignalChannel, type StateEvent, StateEventType } from '../src/index.js';
import { nextFrame } from './utils.js';

use(sinonChai);

function simulateReceivedEvent(connectSubscriptionMock: Subscription<string>, event: StateEvent) {
  const onNextCallback = (connectSubscriptionMock.onNext as sinon.SinonStub).getCall(0).args[0];
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call
  onNextCallback(event);
}

describe('@vaadin/hilla-react-signals', () => {
  describe('NumberSignalChannel', () => {
    let connectClientMock: sinon.SinonStubbedInstance<ConnectClient>;
    let connectSubscriptionMock: Subscription<string>;

    beforeEach(() => {
      connectClientMock = sinon.createStubInstance(ConnectClient);
      connectClientMock.call.resolves();
      connectSubscriptionMock = {
        cancel: sinon.stub(),
        context: sinon.stub().returnsThis(),
        onComplete: sinon.stub().returnsThis(),
        onError: sinon.stub().returnsThis(),
        onNext: sinon.stub().returnsThis(),
      };
      // Mock the subscribe method
      connectClientMock.subscribe.returns(connectSubscriptionMock);
    });

    afterEach(() => {
      sinon.restore();
    });

    it('should create signal instance of type NumberSignal', () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      expect(numberSignalChannel.signal).to.be.instanceOf(NumberSignal);
      expect(numberSignalChannel.signal.value).to.be.undefined;
    });

    it('should subscribe to signal provider endpoint', () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      expect(connectClientMock.subscribe).to.be.have.been.calledOnce;
      expect(connectClientMock.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: numberSignalChannel.id,
        signalProviderEndpointMethod: 'testEndpoint',
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      numberSignalChannel.signal.value = 42;

      expect(connectClientMock.call).to.be.have.been.calledOnce;
      expect(connectClientMock.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: numberSignalChannel.id,
          event: { type: StateEventType.SET, value: 42 },
        },
        undefined,
      );
    });

    it("should update signal's value based on the received event", () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      expect(numberSignalChannel.signal.value).to.be.undefined;

      // Simulate the event received from the server:
      const snapshotEvent: StateEvent = { id: 'someId', type: StateEventType.SNAPSHOT, value: 42 };
      simulateReceivedEvent(connectSubscriptionMock, snapshotEvent);

      // Check if the signal value is updated:
      expect(numberSignalChannel.signal.value).to.equal(42);
    });

    it("should render signal's the updated value", async () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      const numberSignal = numberSignalChannel.signal;
      simulateReceivedEvent(connectSubscriptionMock, { id: 'someId', type: StateEventType.SNAPSHOT, value: 42 });

      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');

      simulateReceivedEvent(connectSubscriptionMock, { id: 'someId', type: StateEventType.SNAPSHOT, value: 99 });
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 99');
    });
  });
});
