import atmosphere from 'atmosphere.js';
import sinon from 'sinon';

export type Events =
  | 'onClientTimeout'
  | 'onClose'
  | 'onError'
  | 'onFailureToReconnect'
  | 'onLocalMessage'
  | 'onMessage'
  | 'onMessagePublished'
  | 'onOpen'
  | 'onReconnect'
  | 'onReopen'
  | 'onTransportFailure';

export const pushStub = sinon.stub();

export const subscribeStub = sinon.stub(atmosphere, 'subscribe').callsFake((request: Atmosphere.Request) => {
  if (subscribeStub.getCalls().length === 1) {
    throw new Error('Atmosphere subscribe called while already subscribed');
  }

  pushStub.resetHistory();

  const ret = {
    push: pushStub,
  };

  request.onOpen?.();

  return ret;
});

export default atmosphere;
