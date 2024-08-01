/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { StateEvent } from '../src';
import { effect } from '../src';
import { NumberSignal } from '../src';
import { nextFrame } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  describe('NumberSignal', () => {
    let publishSpy: sinon.SinonSpy;

    beforeEach(() => {
      publishSpy = sinon.spy(async (_: StateEvent): Promise<boolean> => Promise.resolve(true));
    });

    afterEach(() => {
      sinon.restore();
    });

    it('should retain default value as initialized', () => {
      const numberSignal1 = new NumberSignal(publishSpy);
      expect(numberSignal1.value).to.be.undefined;

      const numberSignal2 = new NumberSignal(publishSpy, undefined);
      expect(numberSignal2.value).to.be.undefined;

      const numberSignal3 = new NumberSignal(publishSpy, 0);
      expect(numberSignal3.value).to.equal(0);

      const numberSignal4 = new NumberSignal(publishSpy, 42.424242);
      expect(numberSignal4.value).to.equal(42.424242);

      const numberSignal5 = new NumberSignal(publishSpy, -42.424242);
      expect(numberSignal5.value).to.equal(-42.424242);
    });

    it('should publish the new value to the server when set', () => {
      const numberSignal = new NumberSignal(publishSpy);
      numberSignal.value = 42;
      expect(publishSpy).to.have.been.calledOnce;
      expect(publishSpy).to.have.been.calledWithMatch({ type: 'set', value: 42 });

      publishSpy.resetHistory();

      const numberSignal2 = new NumberSignal(publishSpy, 0);
      // eslint-disable-next-line no-plusplus
      numberSignal2.value++;
      expect(publishSpy).to.have.been.calledOnce;
      expect(publishSpy).to.have.been.calledWithMatch({ type: 'set', value: 1 });
    });

    it('should render value when signal is rendered', async () => {
      const numberSignal = new NumberSignal(publishSpy, 42);
      const result = render(<span>Value is {numberSignal}</span>);
      await nextFrame();
      expect(result.container.textContent).to.equal('Value is 42');
    });

    it('should set the underlying value locally without waiting for server confirmation', () => {
      const numberSignal = new NumberSignal(publishSpy);
      expect(numberSignal.value).to.equal(undefined);
      numberSignal.value = 42;
      expect(numberSignal.value).to.equal(42);

      const anotherNumberSignal = new NumberSignal(publishSpy);
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
