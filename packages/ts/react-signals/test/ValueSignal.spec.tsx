/* eslint-disable @typescript-eslint/unbound-method */
import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiLike from 'chai-like';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import type { StateEvent } from '../src/events.js';
import type { ServerConnectionConfig } from '../src/FullStackSignal.js';
import { ValueSignal } from '../src/index.js';
import { computed } from '../src/index.js';
import { createSubscriptionStub, nextFrame } from './utils.js';

use(sinonChai);
use(chaiLike);

describe('@vaadin/hilla-react-signals', () => {
  let config: ServerConnectionConfig;
  let subscription: sinon.SinonStubbedInstance<Subscription<StateEvent<string>>>;
  let client: sinon.SinonStubbedInstance<ConnectClient>;
  beforeEach(() => {
    client = sinon.createStubInstance(ConnectClient);
    client.call.resolves();
    // Mock the subscribe method
    subscription = createSubscriptionStub();
    client.subscribe.returns(subscription);
    config = { client, endpoint: 'TestEndpoint', method: 'testMethod' };
  });

  describe('ValueSignal', () => {
    it('should retain default value as initialized', async () => {
      const valueSignal = new ValueSignal<string>('', config);
      const _ = computed(() => valueSignal.value);
      valueSignal.value = 'a';
      console.log(_.value);
      //  render(<div>{_}</div>);
      await nextFrame();
      expect(valueSignal.value).to.be.undefined;
      const [onNextCallback] = subscription.onNext.firstCall.args;
      const promise = valueSignal.update((currValue) => `${currValue} a`);
      const [, , params] = client.call.firstCall.args;

      onNextCallback({ id: params?.event.id, type: 'snapshot', value: 'a' });
      await promise;
    });
  });
});
