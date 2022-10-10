import { expect } from '@open-wc/testing';
import { atmosphere } from 'a-atmosphere-javascript';
import type { ReactiveController } from 'lit';
import { FluxConnection, State } from '../src/FluxConnection';
import type {
  AbstractMessage,
  ClientCompleteMessage,
  ClientErrorMessage,
  ClientUpdateMessage,
} from '../src/FluxMessages';

function expectNoDataRetained(fluxConnectionAny: any) {
  expect(fluxConnectionAny.endpointInfos.size).to.equal(0);
  expect(fluxConnectionAny.onNextCallbacks.size).to.equal(0);
  expect(fluxConnectionAny.onCompleteCallbacks.size).to.equal(0);
  expect(fluxConnectionAny.onErrorCallbacks.size).to.equal(0);
}

describe('FluxConnection', () => {
  let fluxConnection: FluxConnection;
  let fluxConnectionHelper: {
    nrSentMessages(): number;
    sentMessage(i: number): AbstractMessage;
    handleMessage(msg: AbstractMessage): unknown;
    socket: () => any;
  };

  beforeEach(() => {
    (window as any).Vaadin = { featureFlags: { hillaPush: true } }; // Remove when removing feature flag
    fluxConnection = new FluxConnection();
    const socket = () => (fluxConnection as any).socket;
    (atmosphere as any).reset();
    fluxConnectionHelper = {
      socket,
      handleMessage(msg) {
        socket().fakeEvent('onMessage', { responseBody: JSON.stringify(msg) });
      },
      sentMessage(i) {
        return JSON.parse(socket().sentMessages[i]);
      },
      nrSentMessages() {
        return socket().sentMessages.length;
      },
    };
  });

  it('should be exported', () => {
    expect(FluxConnection).to.be.ok;
  });
  it('requires a feature flag', () => {
    delete (window as any).Vaadin;
    try {
      new FluxConnection(); // eslint-disable-line no-new
      expect.fail('Should not work without a feature flag');
    } catch (e) {
      // Just to ensure something is thrown
    }
  });

  it('should establish a websocket connection when using an endpoint', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    expect(fluxConnectionHelper.socket()).not.to.equal(undefined);
  });

  it('should reuse the websocket connection for all endpoints', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    const socket = fluxConnectionHelper.socket();
    fluxConnection.subscribe('OtherEndpoint', 'otherMethod');
    expect(fluxConnectionHelper.socket()).to.equal(socket);
  });

  it('should send a subscribe server message when subscribing', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    expect(fluxConnectionHelper.sentMessage(0)).to.eql({
      '@type': 'subscribe',
      id: '0',
      endpointName: 'MyEndpoint',
      methodName: 'myMethod',
      params: [],
    });
  });

  it('should immediately return a Subscription when subscribing', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    expect(sub).to.not.equal(undefined);
    expect(sub.onNext).to.not.equal(undefined);
    expect(sub.onComplete).to.not.equal(undefined);
    expect(sub.onError).to.not.equal(undefined);
  });

  it('should call onNext when receiving a server message', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    const receivedValues: any[] = [];
    sub.onNext((value) => {
      receivedValues.push(value);
    });
    const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
    fluxConnectionHelper.handleMessage(msg);
    expect(receivedValues.length).to.equal(1);
    expect(receivedValues[0]).to.eql({ foo: 'bar' });
  });
  it('should call onComplete when receiving a server message', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    let completeCalled = 0;
    sub.onComplete(() => {
      completeCalled += 1;
    });
    const msg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
    fluxConnectionHelper.handleMessage(msg);
    expect(completeCalled).to.eq(1);
  });
  it('should call onError when receiving a server message', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    let errorCalled = 0;
    sub.onError(() => {
      errorCalled += 1;
    });
    const msg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'it failed' };
    fluxConnectionHelper.handleMessage(msg);
    expect(errorCalled).to.eq(1);
  });
  it('should not deliver messages after completing', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    let onNextCalled = 0;
    sub.onNext((_value) => {
      onNextCalled += 1;
    });
    const completeMsg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
    const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
    fluxConnectionHelper.handleMessage(completeMsg);
    try {
      fluxConnectionHelper.handleMessage(msg);
      expect.fail('Should not fail silently');
    } catch (e) {
      // No need to handle the error
    }
    expect(onNextCalled).to.eq(0);
  });
  it('should cancel the server subscription on cancel', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onNext((_value) => {
      // No need to handle the value
    });
    sub.cancel();
    expect(fluxConnectionHelper.nrSentMessages()).to.equal(2);
    expect(fluxConnectionHelper.sentMessage(1)).to.eql({
      '@type': 'unsubscribe',
      id: '0',
    });
  });
  it('should not deliver messages after canceling', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    let onNextCalled = 0;
    sub.onNext((_value) => {
      onNextCalled += 1;
    });
    sub.cancel();
    const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
    fluxConnectionHelper.handleMessage(msg);
    expect(onNextCalled).to.equal(0);
  });
  it('should throw an error for messages to unknown subscriptions', () => {
    const msg: ClientUpdateMessage = { '@type': 'update', id: '0', item: { foo: 'bar' } };
    try {
      fluxConnectionHelper.handleMessage(msg);
      expect.fail('Should have thrown an error');
    } catch (e) {
      // No need to handle the error
    }
  });
  it('should throw an error for flux errors without onError', () => {
    const msg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'foo' };
    try {
      fluxConnectionHelper.handleMessage(msg);
      expect.fail('Should have thrown an error');
    } catch (e) {
      // No need to handle the error
    }
  });
  it('should throw an error for unknown messages', () => {
    const msg: any = { '@type': 'unknown', id: '0' };
    try {
      fluxConnectionHelper.handleMessage(msg);
      expect.fail('Should have thrown an error');
    } catch (e) {
      // No need to handle the error
    }
  });
  it('clean internal data on complete', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onComplete(() => {
      // Just need a callback
    });
    sub.onError(() => {
      // Just need a callback
    });
    sub.onNext((_value) => {
      // Just need a callback
    });

    const completeMsg: ClientCompleteMessage = { '@type': 'complete', id: '0' };
    fluxConnectionHelper.handleMessage(completeMsg);

    expectNoDataRetained(fluxConnection);
  });
  it('clean internal data on error', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onComplete(() => {
      // Just need a callback
    });
    sub.onError(() => {
      // Just need a callback
    });
    sub.onNext((_value) => {
      // Just need a callback
    });

    const completeMsg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'foo' };
    fluxConnectionHelper.handleMessage(completeMsg);

    expectNoDataRetained(fluxConnection);
  });
  it('clean internal data on cancel', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onComplete(() => {
      // Just need a callback
    });
    sub.onError(() => {
      // Just need a callback
    });
    sub.onNext((_value) => {
      // Just need a callback
    });
    sub.cancel();

    expectNoDataRetained(fluxConnection);
  });
  it('should ignore a second cancel call', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onComplete(() => {
      // Just need a callback
    });
    sub.onError(() => {
      // Just need a callback
    });
    sub.onNext((_value) => {
      // Just need a callback
    });
    expect(fluxConnectionHelper.nrSentMessages()).to.equal(1);

    sub.cancel();
    expect(fluxConnectionHelper.nrSentMessages()).to.equal(2);
    sub.cancel();
    expect(fluxConnectionHelper.nrSentMessages()).to.equal(2);
  });
  it('calls cancel when context is deactivated', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    sub.onComplete(() => {
      // Just need a callback
    });
    sub.onError(() => {
      // Just need a callback
    });
    sub.onNext((_value) => {
      // Just need a callback
    });
    class FakeElement {
      private controllers: ReactiveController[] = [];

      addController(controller: ReactiveController) {
        this.controllers.push(controller);
      }
      disconnectedCallback() {
        this.controllers.forEach((controller) => controller.hostDisconnected && controller.hostDisconnected());
      }
    }

    const fakeElement: any = new FakeElement();
    sub.context(fakeElement);
    fakeElement.disconnectedCallback();
    expectNoDataRetained(fluxConnection);
  });
  it('dispatches an active event on Atmosphere connect', () => {
    fluxConnection.state = State.INACTIVE;
    let events = 0;
    fluxConnection.addEventListener('state-changed', (e) => {
      if (e.detail.active) {
        events += 1;
      }
    });
    fluxConnectionHelper.socket().fakeEvent('onOpen');
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
    fluxConnectionHelper.socket().fakeEvent('onOpen');
    fluxConnectionHelper.socket().fakeEvent('onClose');
    fluxConnectionHelper.socket().fakeEvent('onReopen');
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
    fluxConnectionHelper.socket().fakeEvent('onOpen');
    fluxConnectionHelper.socket().fakeEvent('onClose');
    expect(events).to.equal(1);
  });
});
