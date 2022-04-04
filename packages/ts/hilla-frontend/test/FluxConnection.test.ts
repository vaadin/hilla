import { expect } from '@open-wc/testing';
import { FluxConnection } from '../src/FluxConnection';
import type { ClientCompleteMessage, ClientErrorMessage, ClientUpdateMessage } from '../src/FluxMessages';

const base = window.location.origin;

describe('FluxConnection', () => {
  let fluxConnection: FluxConnection;
  let fluxConnectionAny: any;

  beforeEach(() => {
    fluxConnection = new FluxConnection();
    fluxConnectionAny = fluxConnection;
  });

  afterEach(() => {});

  it('should be exported', () => {
    expect(FluxConnection).to.be.ok;
  });

  it('should establish a websocket connection when using an endpoint', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    expect(fluxConnectionAny.socket).not.to.equal(undefined);
  });

  it('should reuse the websocket connection for all endpoints', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    const socket = fluxConnectionAny.socket;
    fluxConnection.subscribe('OtherEndpoint', 'otherMethod');
    expect(fluxConnectionAny.socket).to.equal(socket);
  });

  it('should send a subscribe server message when subscribing', () => {
    fluxConnection.subscribe('MyEndpoint', 'myMethod');
    expect(fluxConnectionAny.socket.sentMessages[0]).to.eql({
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
    fluxConnectionAny.handleMessage(msg);
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
    fluxConnectionAny.handleMessage(msg);
    expect(completeCalled).to.eq(1);
  });
  it('should call onError when receiving a server message', () => {
    const sub = fluxConnection.subscribe('MyEndpoint', 'myMethod');
    let errorCalled = 0;
    sub.onError(() => {
      errorCalled += 1;
    });
    const msg: ClientErrorMessage = { '@type': 'error', id: '0', message: 'it failed' };
    fluxConnectionAny.handleMessage(msg);
    expect(errorCalled).to.eq(1);
  });
});
