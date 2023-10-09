import { expect } from '@esm-bundle/chai';
import type { ReactiveControllerHost, ReactiveController } from '@lit/reactive-element';
import sinon from 'sinon';
import { FluxConnection, State } from '../src/FluxConnection.js';
import type {
  AbstractMessage,
  ClientCompleteMessage,
  ClientErrorMessage,
  ClientUpdateMessage,
} from '../src/FluxMessages.js';
import { getSubscriptionEventSpies, subscribeStub } from './mocks/atmosphere.js';

describe('@hilla/frontend', () => {
  describe('FluxConnection', () => {
    function emitMessage(msg: AbstractMessage) {
      getSubscriptionEventSpies()?.onMessage?.({ responseBody: JSON.stringify(msg) });
    }

    function getLastEmittedMessage(): AbstractMessage | undefined {
      const [message] = getSubscriptionEventSpies()?.push?.lastCall.args ?? [];
      return message ? JSON.parse(message) : undefined;
    }

    function getEmittedMessagesCount(): number {
      return getSubscriptionEventSpies()?.push?.callCount ?? 0;
    }

    let fluxConnection: FluxConnection;
    let fluxConnectionHelper: {
      nrSentMessages(): number;
      sentMessage(i: number): AbstractMessage | undefined;
      handleMessage(msg: AbstractMessage): unknown;
    };

    beforeEach(() => {
      subscribeStub.resetHistory();
      fluxConnection = new FluxConnection('/connect');
      fluxConnectionHelper = {
        handleMessage(msg) {
          getSubscriptionEventSpies()?.onMessage?.({ responseBody: JSON.stringify(msg) });
        },
        nrSentMessages() {
          return getSubscriptionEventSpies()?.push?.callCount ?? 0;
        },
        sentMessage(i) {
          const arg = getSubscriptionEventSpies()?.push?.lastCall.args[i];
          return arg ? JSON.parse(arg) : undefined;
        },
      };
    });

    it('should be exported', () => {
      expect(FluxConnection).to.be.ok;
    });

    it('should establish a websocket connection when using an endpoint', () => {
      const endpointName = 'MyEndpoint';
      const methodName = 'myMethod';
      fluxConnection.subscribe(endpointName, methodName);
      expect(getSubscriptionEventSpies()?.push).to.have.been.calledWith(
        JSON.stringify({
          '@type': 'subscribe',
          endpointName,
          id: '0',
          methodName,
          params: [],
        }),
      );
    });

    it('should reuse the websocket connection for all endpoints', () => {
      fluxConnection.subscribe('MyEndpoint', 'myMethod');
      fluxConnection.subscribe('OtherEndpoint', 'otherMethod');
      expect(subscribeStub).to.have.been.calledOnce;
    });

    it('should immediately return a Subscription when subscribing', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      for (const name of ['onNext', 'onComplete', 'onError']) {
        expect(sub).to.have.property(name).which.is.a('function');
      }
    });

    it('should call onNext when receiving a server message', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const receivedValues: any[] = [];
      sub.onNext((value: any) => {
        receivedValues.push(value);
      });
      const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
      emitMessage(msg);
      expect(receivedValues.length).to.equal(1);
      expect(receivedValues[0]).to.eql({ foo: 'bar' });
    });

    it('should call onComplete when receiving a server message', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onComplete = sinon.stub();
      sub.onComplete(onComplete);
      const msg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
      emitMessage(msg);
      expect(onComplete).to.have.been.calledOnce;
    });

    it('should call onError when receiving a server message', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onError = sinon.stub();
      sub.onError(onError);
      const msg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'it failed' };
      emitMessage(msg);
      expect(onError).to.have.been.calledOnce;
    });

    it('should not deliver messages after completing', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onNext = sinon.stub();
      sub.onNext(onNext);
      const completeMsg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
      const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
      emitMessage(completeMsg);
      expect(() => emitMessage(msg)).to.throw;
      expect(onNext).to.have.not.been.called;
    });

    it('should cancel the server subscription on cancel', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      sub.onNext(sinon.stub());
      sub.cancel();
      expect(getEmittedMessagesCount()).to.equal(2);
      expect(getLastEmittedMessage()).to.eql({
        '@type': 'unsubscribe',
        id: '0',
      });
    });

    it('should not deliver messages after canceling', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onNext = sinon.stub();
      sub.onNext(onNext);
      sub.cancel();
      const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
      emitMessage(msg);
      expect(onNext).to.have.not.been.called;
    });

    it('should throw an error for messages to unknown subscriptions', () => {
      const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
      expect(() => emitMessage(msg)).to.throw;
    });

    it('should throw an error for flux errors without onError', () => {
      const msg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'foo' };
      expect(() => emitMessage(msg)).to.throw;
    });

    it('should throw an error for unknown messages', () => {
      expect(() => emitMessage({ '@type': 'unknown', id: '0' })).to.throw;
    });

    it('clean internal data on complete', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onComplete = sinon.stub();
      const onError = sinon.stub();
      const onNext = sinon.stub();

      sub.onComplete(onComplete);
      sub.onError(onError);
      sub.onNext(onNext);

      const completeMsg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
      emitMessage(completeMsg);

      emitMessage({ '@type': 'update', id: '0' });
      expect(onNext).to.have.not.been.called;

      expect(() => emitMessage({ '@type': 'error', id: '0' })).to.throw;
      expect(onError).to.have.not.been.called;

      emitMessage(completeMsg);
      expect(onComplete).to.have.been.calledOnce;
    });

    it('clean internal data on error', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onComplete = sinon.stub();
      const onError = sinon.stub();
      const onNext = sinon.stub();

      sub.onComplete(onComplete);
      sub.onError(onError);
      sub.onNext(onNext);

      const errorMsg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'foo' };
      emitMessage(errorMsg);

      emitMessage({ '@type': 'update', id: '0' });
      expect(onNext).to.have.not.been.called;

      expect(() => emitMessage(errorMsg)).to.throw;
      expect(onError).to.have.been.calledOnce;

      emitMessage({ '@type': 'complete', id: '0' });
      expect(onComplete).to.have.not.been.called;
    });

    it('clean internal data on cancel', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onComplete = sinon.stub();
      const onError = sinon.stub();
      const onNext = sinon.stub();

      sub.onComplete(onComplete);
      sub.onError(onError);
      sub.onNext(onNext);

      sub.cancel();

      emitMessage({ '@type': 'update', id: '0' });
      expect(onNext).to.have.not.been.called;

      expect(() => emitMessage({ '@type': 'error', id: '0' })).to.throw;
      expect(onError).to.have.not.been.called;

      emitMessage({ '@type': 'complete', id: '0' });
      expect(onComplete).to.have.not.been.called;
    });

    it('should ignore a second cancel call', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      sub.onComplete(sinon.stub());
      sub.onError(sinon.stub());
      sub.onNext(sinon.stub());
      expect(getEmittedMessagesCount()).to.equal(1);

      sub.cancel();
      expect(getEmittedMessagesCount()).to.equal(2);
      sub.cancel();
      expect(getEmittedMessagesCount()).to.equal(2);
    });

    it('calls cancel when context is deactivated', () => {
      const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
      const onComplete = sinon.stub();
      const onError = sinon.stub();
      const onNext = sinon.stub();

      sub.onComplete(onComplete);
      sub.onError(onError);
      sub.onNext(onNext);

      class FakeElement implements ReactiveControllerHost {
        declare removeController: (_: ReactiveController) => void;
        declare requestUpdate: () => void;
        declare readonly updateComplete: Promise<boolean>;
        private readonly controllers: ReactiveController[] = [];

        addController(controller: ReactiveController) {
          this.controllers.push(controller);
        }

        disconnectedCallback() {
          this.controllers.forEach((controller) => controller.hostDisconnected?.());
        }
      }

      const fakeElement = new FakeElement();
      sub.context(fakeElement);
      fakeElement.disconnectedCallback();

      emitMessage({ '@type': 'update', id: '0' });
      expect(onNext).to.have.not.been.called;

      expect(() => emitMessage({ '@type': 'error', id: '0' })).to.throw;
      expect(onError).to.have.not.been.called;

      emitMessage({ '@type': 'complete', id: '0' });
      expect(onComplete).to.have.not.been.called;
    });

    it('dispatches an active event on Atmosphere connect', () => {
      fluxConnection.state = State.INACTIVE;
      let events = 0;
      fluxConnection.addEventListener('state-changed', (e) => {
        if (e.detail.active) {
          events += 1;
        }
      });
      getSubscriptionEventSpies()?.onOpen?.();
      expect(events).to.equal(1);
    });

    it('dispatches an active event on Atmosphere reconnect', () => {
      fluxConnection.state = State.INACTIVE;
      let events = 0;
      fluxConnection.addEventListener('state-changed', (e) => {
        if (e.detail.active) {
          events += 1;
        }
      });
      getSubscriptionEventSpies()?.onOpen?.();
      getSubscriptionEventSpies()?.onClose?.();
      getSubscriptionEventSpies()?.onReopen?.();
      expect(events).to.equal(2);
    });

    it('dispatches an inactive event on Atmosphere disconnect', () => {
      fluxConnection.state = State.INACTIVE;
      let events = 0;
      fluxConnection.addEventListener('state-changed', (e) => {
        if (!e.detail.active) {
          events += 1;
        }
      });
      getSubscriptionEventSpies()?.onOpen?.();
      getSubscriptionEventSpies()?.onClose?.();
      expect(events).to.equal(1);
    });

    it('by default it should use the default url', () => {
      subscribeStub.resetHistory();
      fluxConnection = new FluxConnection('/connect');
      expect(subscribeStub.lastCall.firstArg).to.have.property('url').which.equals('HILLA/push');
    });

    it('should use a custom prefix when connecting', () => {
      subscribeStub.resetHistory();
      fluxConnection = new FluxConnection('/custom/connect');
      expect(subscribeStub.lastCall.firstArg).to.have.property('url').which.equals('/custom/HILLA/push');
    });
  });
});
