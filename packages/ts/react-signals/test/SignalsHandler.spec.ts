/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { ConnectClient } from '@vaadin/hilla-frontend';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { type StateEvent, StateEventType } from '../src/index.js';
import SignalsHandler from '../src/SignalsHandler.js';

use(sinonChai);

describe('@vaadin/hilla-react-signals', () => {
  describe('signalsHandler', () => {
    let connectClientMock: sinon.SinonStubbedInstance<ConnectClient>;
    let signalsHandler: SignalsHandler;

    beforeEach(() => {
      connectClientMock = sinon.createStubInstance(ConnectClient);
      signalsHandler = new SignalsHandler(connectClientMock);
    });

    afterEach(() => {
      sinon.restore();
    });

    it('subscribe should call client.subscribe', () => {
      const signalProviderEndpointMethod = 'testEndpoint';
      const clientSignalId = 'testSignalId';
      signalsHandler.subscribe(signalProviderEndpointMethod, clientSignalId);

      expect(connectClientMock.subscribe).to.be.have.been.calledOnce;
      expect(connectClientMock.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        signalProviderEndpointMethod,
        clientSignalId,
      });
    });

    it('update should call client.call', async () => {
      const clientSignalId = 'testSignalId';
      const event: StateEvent = { id: 'testEvent', type: StateEventType.SET, value: 10 };
      const init = {};

      await signalsHandler.update(clientSignalId, event, init);

      expect(connectClientMock.call).to.be.have.been.calledOnce;
      expect(connectClientMock.call).to.have.been.calledWith(
        'SignalsHandler',
        'update',
        { clientSignalId, event },
        init,
      );
    });
  });
});
