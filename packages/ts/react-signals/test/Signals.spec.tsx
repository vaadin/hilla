/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { effect } from '../src';
import { NumberSignal } from '../src';
import type { ServerConnectionConfig, StateEvent } from '../src/FullStackSignal.js';
import { nextFrame } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  let config: ServerConnectionConfig;

  beforeEach(() => {
    const client = sinon.createStubInstance(ConnectClient);
    client.call.resolves();

    const subscription = sinon.spy<Subscription<StateEvent<number>>>({
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
      onDisconnect() {
        return this;
      },
    });
    // Mock the subscribe method
    client.subscribe.returns(subscription);
    config = { client, endpoint: 'TestEndpoint', method: 'testMethod' };
  });

  describe('NumberSignal', () => {
    it('should retain default value as initialized', () => {
      const numberSignal1 = new NumberSignal(undefined, config);
      expect(numberSignal1.value).to.be.undefined;

      const numberSignal2 = new NumberSignal(0, config);
      expect(numberSignal2.value).to.equal(0);

      const numberSignal3 = new NumberSignal(42.424242, config);
      expect(numberSignal3.value).to.equal(42.424242);

      const numberSignal4 = new NumberSignal(-42.424242, config);
      expect(numberSignal4.value).to.equal(-42.424242);
    });

    it('should render value when signal is rendered', async () => {
      const numberSignal = new NumberSignal(42, config);
      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');
    });

    it('should set the underlying value locally without waiting for server confirmation', () => {
      const numberSignal = new NumberSignal(undefined, config);
      expect(numberSignal.value).to.equal(undefined);
      numberSignal.value = 42;
      expect(numberSignal.value).to.equal(42);

      const anotherNumberSignal = new NumberSignal(undefined, config);
      const results: Array<number | undefined> = [];

      effect(() => {
        results.push(anotherNumberSignal.value);
      });
      anotherNumberSignal.value = 42;
      anotherNumberSignal.value += 1;

      expect(results).to.be.like([undefined, 42, 43]);
    });
  });
});
