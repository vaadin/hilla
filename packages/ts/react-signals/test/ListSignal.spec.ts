import { expect } from '@esm-bundle/chai';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import { ListSignal } from '../src';
import type { StateEvent } from '../src/events.js';
import { createSubscriptionStub, subscribeToSignalViaEffect } from './utils.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('ListSignal', () => {
    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent>>;
    let listSignal: ListSignal<string>;

    function simulateReceivingAcceptedEvent(event: StateEvent): void {
      const [onNextCallback] = subscription.onNext.firstCall.args;
      onNextCallback({ ...event, accepted: true });
    }

    beforeEach(() => {
      client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();
      subscription = createSubscriptionStub();
      client.subscribe.returns(subscription);
      listSignal = new ListSignal({ client, endpoint: 'NameService', method: 'nameListSignal' });
    });

    it('should create a new ListSignal instance', () => {
      expect(listSignal).to.be.an.instanceOf(ListSignal);
    });

    it('should have empty items array by default', () => {
      expect(listSignal.value).to.be.an('array').that.is.empty;
    });

    it('should not subscribe to signal provider endpoint before being subscribed to', () => {
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to server side instance when it is subscribed to on client side', () => {
      subscribeToSignalViaEffect(listSignal);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).to.have.been.calledOnce;
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: listSignal.id,
        providerEndpoint: 'NameService',
        providerMethod: 'nameListSignal',
        params: undefined,
        parentClientSignalId: undefined,
      });
    });

    it('should be able to set value internally', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', value: 'Bob' },
        ],
      };
      simulateReceivingAcceptedEvent(snapshot);
      expect(listSignal.value).to.have.length(2);
    });
  });
});
