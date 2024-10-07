// eslint-disable-next-line import/no-unassigned-import
import './setup.js';

import {expect} from '@esm-bundle/chai';
import {createIncrementStateEvent, createReplaceStateEvent, createSetStateEvent} from '../src/events.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('CreateStateEventType', () => {
    it('should create correct SetStateEvent', () => {
      const setStateEvent = createSetStateEvent('foo');
      expect(setStateEvent.id).to.be.not.null;
      expect(setStateEvent.type).to.equal('set');
      expect(setStateEvent.value).to.equal('foo');
    });

    it('should create correct ReplaceStateEvent', () => {
      const setStateEvent = createReplaceStateEvent('foo', 'bar');
      expect(setStateEvent.id).to.be.not.null;
      expect(setStateEvent.type).to.equal('replace');
      expect(setStateEvent.value).to.equal('bar');
      expect(setStateEvent.expected).to.equal('foo');
    });

    it('should create correct IncrementStateEvent', () => {
      const incrementStateEvent = createIncrementStateEvent(42);
      expect(incrementStateEvent.id).to.be.not.null;
      expect(incrementStateEvent.type).to.equal('increment');
      expect(incrementStateEvent.value).to.equal(42);
    });
  });
});
