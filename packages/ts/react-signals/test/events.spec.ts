import { expect } from 'chai';
import {
  createIncrementStateEvent,
  createInsertLastStateEvent,
  createRemoveStateEvent,
  createReplaceStateEvent,
  createSetStateEvent,
  isIncrementStateEvent,
  isInsertLastStateEvent,
  isListSnapshotStateEvent,
  isRemoveStateEvent,
  isReplaceStateEvent,
  isSetStateEvent,
  isSnapshotStateEvent,
  type ListEntry,
} from '../src/events.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('CreateStateEventType', () => {
    it('should create correct SetStateEvent', () => {
      const setStateEvent1 = createSetStateEvent('foo');
      expect(setStateEvent1.id).to.be.not.null;
      expect(setStateEvent1.type).to.equal('set');
      expect(setStateEvent1.value).to.equal('foo');
      expect(setStateEvent1.parentSignalId).not.to.exist;
      expect(setStateEvent1.accepted).to.be.false;
      expect(isSetStateEvent(setStateEvent1)).to.be.true;

      const setStateEvent2 = createSetStateEvent('bar', '123');
      expect(setStateEvent2.id).to.equal('123');
      expect(setStateEvent2.type).to.equal('set');
      expect(setStateEvent2.value).to.equal('bar');
      expect(setStateEvent2.parentSignalId).not.to.exist;
      expect(setStateEvent2.accepted).to.be.false;
      expect(isSetStateEvent(setStateEvent2)).to.be.true;

      const setStateEvent3 = createSetStateEvent('baz', '456', '789');
      expect(setStateEvent3.id).to.equal('456');
      expect(setStateEvent3.type).to.equal('set');
      expect(setStateEvent3.value).to.equal('baz');
      expect(setStateEvent3.parentSignalId).to.equal('789');
      expect(setStateEvent3.accepted).to.be.false;
      expect(isSetStateEvent(setStateEvent3)).to.be.true;
    });

    it('should create correct ReplaceStateEvent', () => {
      const replaceStateEvent1 = createReplaceStateEvent('foo', 'bar');
      expect(replaceStateEvent1.id).to.be.not.null;
      expect(replaceStateEvent1.type).to.equal('replace');
      expect(replaceStateEvent1.value).to.equal('bar');
      expect(replaceStateEvent1.expected).to.equal('foo');
      expect(replaceStateEvent1.parentSignalId).not.to.exist;
      expect(replaceStateEvent1.accepted).to.be.false;
      expect(isSetStateEvent(replaceStateEvent1)).to.be.false;
      expect(isReplaceStateEvent(replaceStateEvent1)).to.be.true;

      const replaceStateEvent2 = createReplaceStateEvent('foo', 'bar', '123');
      expect(replaceStateEvent2.id).to.be.equal('123');
      expect(replaceStateEvent2.type).to.equal('replace');
      expect(replaceStateEvent2.value).to.equal('bar');
      expect(replaceStateEvent2.expected).to.equal('foo');
      expect(replaceStateEvent2.parentSignalId).not.to.exist;
      expect(replaceStateEvent2.accepted).to.be.false;
      expect(isSetStateEvent(replaceStateEvent2)).to.be.false;
      expect(isReplaceStateEvent(replaceStateEvent2)).to.be.true;

      const replaceStateEvent3 = createReplaceStateEvent('foo', 'bar', '123', '456');
      expect(replaceStateEvent3.id).to.be.equal('123');
      expect(replaceStateEvent3.type).to.equal('replace');
      expect(replaceStateEvent3.value).to.equal('bar');
      expect(replaceStateEvent3.expected).to.equal('foo');
      expect(replaceStateEvent3.parentSignalId).to.equal('456');
      expect(replaceStateEvent3.accepted).to.be.false;
      expect(isSetStateEvent(replaceStateEvent3)).to.be.false;
      expect(isReplaceStateEvent(replaceStateEvent3)).to.be.true;
    });

    it('should create correct IncrementStateEvent', () => {
      const incrementStateEvent = createIncrementStateEvent(42);
      expect(incrementStateEvent.id).to.be.not.null;
      expect(incrementStateEvent.type).to.equal('increment');
      expect(incrementStateEvent.value).to.equal(42);
      expect(incrementStateEvent.accepted).to.be.false;
      expect(isSetStateEvent(incrementStateEvent)).to.be.false;
      expect(isReplaceStateEvent(incrementStateEvent)).to.be.false;
      expect(isIncrementStateEvent(incrementStateEvent)).to.be.true;
    });

    it('should create correct InsertLastStateEvent', () => {
      const insertLastStateEvent = createInsertLastStateEvent('foo');
      expect(insertLastStateEvent.id).to.be.not.null;
      expect(insertLastStateEvent.type).to.equal('insert');
      expect(insertLastStateEvent.value).to.equal('foo');
      expect(insertLastStateEvent.position).to.equal('last');
      expect(insertLastStateEvent.entryId).not.to.exist;
      expect(insertLastStateEvent.accepted).to.be.false;
      expect(isInsertLastStateEvent(insertLastStateEvent)).to.be.true;
    });

    it('should create correct RemoveStateEvent', () => {
      const removeStateEvent = createRemoveStateEvent('a1b2c3e4');
      expect(removeStateEvent.id).to.be.not.null;
      expect(removeStateEvent.type).to.equal('remove');
      expect(removeStateEvent.entryId).to.equal('a1b2c3e4');
      expect(removeStateEvent.value).not.to.exist;
      expect(removeStateEvent.accepted).to.be.false;
      expect(isRemoveStateEvent(removeStateEvent)).to.be.true;
    });

    it('should return true only when isSnapshotStateEvent is called on an instance of SnapshotStateEvent', () => {
      const event1 = {
        id: '1',
        type: 'snapshot',
        value: 42,
        accepted: true,
      };
      expect(isSnapshotStateEvent<number>(event1)).to.be.true;

      const event2 = {
        id: '1',
        type: 'set',
        value: 42,
        accepted: true,
      };
      expect(isSnapshotStateEvent<number>(event2)).to.be.false;

      const event3 = {
        id: '1',
        type: 'snapshot',
        value: 42,
      };
      expect(isSnapshotStateEvent<number>(event3)).to.be.false;

      const event4 = {
        id: '1',
        type: 'snapshot',
        accepted: true,
      };
      expect(isSnapshotStateEvent<number>(event4)).to.be.false;
    });

    it('should return true only when isListSnapshotStateEvent is called on an instance of ListSnapshotStateEvent', () => {
      const entries1: Array<ListEntry<number>> = [
        { id: 'entry1', value: 1 },
        { id: 'entry2', value: 2, prev: 'entry1' },
      ];
      const event1 = {
        id: 'list1',
        type: 'snapshot',
        entries: entries1,
        accepted: true,
      };
      expect(isListSnapshotStateEvent<number>(event1)).to.be.true;

      // wrong type
      const event2 = {
        id: 'list1',
        type: 'update',
        entries: entries1,
        accepted: true,
      };
      expect(isListSnapshotStateEvent<number>(event2)).to.be.false;

      // entries not an array
      const event3 = {
        id: 'list1',
        type: 'snapshot',
        entries: 'not-an-array',
        accepted: true,
      };
      expect(isListSnapshotStateEvent<number>(event3)).to.be.false;

      // missing entries
      const event4 = {
        id: 'list1',
        type: 'snapshot',
        accepted: true,
      };
      expect(isListSnapshotStateEvent<number>(event4)).to.be.false;

      // missing accepted
      const event5 = {
        id: 'list1',
        type: 'snapshot',
        entries: entries1,
      };
      expect(isListSnapshotStateEvent<number>(event5)).to.be.false;

      // event is null or not an object
      expect(isListSnapshotStateEvent<number>(null)).to.be.false;
      expect(isListSnapshotStateEvent<number>(undefined)).to.be.false;
      expect(isListSnapshotStateEvent<number>('not-an-object')).to.be.false;
      expect(isListSnapshotStateEvent<number>(true)).to.be.false;
    });
  });
});
