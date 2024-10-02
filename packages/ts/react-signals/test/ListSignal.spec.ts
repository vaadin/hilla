import { expect } from '@esm-bundle/chai';
import { ConnectClient } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import { ListSignal } from '../src';
import { createSubscriptionStub, subscribeToSignalViaEffect } from './utils.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('ListSignal', () => {
    it('should create correct ListSignal', () => {
      const client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();
      // Mock the subscribe method
      const subscription = createSubscriptionStub();
      client.subscribe.returns(subscription);
      const listSignal = new ListSignal({ client, endpoint: 'PersonService', method: 'personListSignal' });
      subscribeToSignalViaEffect(listSignal);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).to.have.been.calledOnce;
    });
  });
});
