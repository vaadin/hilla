// eslint-disable-next-line import/no-unassigned-import
import './setup.js';

import { expect } from '@esm-bundle/chai';
import sinon from 'sinon';
import { DependencyTrackingSignal } from '../src/FullStackSignal.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('Feature-flag Expected Error', () => {
    beforeEach(() => {
      window.Vaadin = {
        featureFlags: {
          fullstackSignals: false,
        },
      };
    });
    afterEach(() => {
      window.Vaadin = {
        featureFlags: {
          fullstackSignals: true,
        },
      };
      sinon.resetHistory();
    });
    const expectedError =
      'The Hilla Fullstack Signals API is currently considered experimental and may change in the future. To use it you need to explicitly enable it in Copilot or by adding com.vaadin.experimental.fullstackSignals=true to vaadin-featureflags.properties';
    class TestSignal<T = unknown> extends DependencyTrackingSignal<T> {
      constructor(value: T | undefined, onSubscribe: () => void, onUnsubscribe: () => void) {
        super(value, onSubscribe, onUnsubscribe);
        this.subscribe(() => {}); // Ignores the internal subscription.
      }
    }
    it('should throw Error when feature-flag is not enabled', () => {
      const onFirstSubscribe = sinon.stub();
      const onLastUnsubscribe = sinon.stub();
      expect(() => {
        const _ = new TestSignal<unknown>(undefined, onFirstSubscribe, onLastUnsubscribe);
      }).to.throw(expectedError);
    });
  });
});
