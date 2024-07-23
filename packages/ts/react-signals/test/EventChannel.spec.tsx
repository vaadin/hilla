/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { StateEvent } from '../src';
import { NumberSignal, NumberSignalChannel } from '../src';
import type {SetEvent, SnapshotEvent} from '../types';

use(sinonChai);

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
      expect(connectClientMock.subscribe).to.have.been.calledWithMatch('SignalsHandler', 'subscribe', {
        signalProviderEndpointMethod: 'testEndpoint',
      });
    });

    it('should publish updates to signals handler endpoint', () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      numberSignalChannel.signal.value = 42;
      const init = {};
      expect(connectClientMock.call).to.be.have.been.calledOnce;
    });

    it('should update signals based on the received event', () => {
      const numberSignalChannel = new NumberSignalChannel('testEndpoint', connectClientMock);
      expect(numberSignalChannel.signal.value).to.be.undefined;
      const snapshotEvent: SnapshotEvent = { id: 'someId', type: 'snapshot', value: 42 };
      // Simulate the event received from the server:
      const onNextCallback = (connectSubscriptionMock.onNext as sinon.SinonStub).getCall(0).args[0];
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      onNextCallback(JSON.stringify(snapshotEvent));
      // Check if the signal value is updated:
      expect(numberSignalChannel.signal.value).to.equal(42);
    });
  });
});
