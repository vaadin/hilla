import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import { expect } from 'chai';
import sinon from 'sinon';
import { ListSignal, ValueSignal } from '../src';
import type { InsertLastStateEvent, RemoveStateEvent, StateEvent } from '../src/events.js';
import { createSubscriptionStub, subscribeToSignalViaEffect } from './utils.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('ListSignal', () => {
    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<StateEvent>>;
    let listSignal: ListSignal<string>;

    function simulateReceivingEvent(event: StateEvent): void {
      const [onNextCallback] = subscription.onNext.firstCall.args;
      onNextCallback(event);
    }

    beforeEach(() => {
      client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();
      subscription = createSubscriptionStub();
      client.subscribe.returns(subscription);
      listSignal = new ListSignal({ client, endpoint: 'NameService', method: 'nameListSignal' });
    });

    it('should create a new ListSignal instance', () => {
      expect(listSignal).to.be.an.instanceOf(ListSignal);
    });

    it('should have empty items array by default', () => {
      expect(listSignal.value).to.be.an('array').that.is.empty;
    });

    it('should not subscribe to signal provider endpoint before being subscribed to', () => {
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to server side instance when it is subscribed to on client side', () => {
      subscribeToSignalViaEffect(listSignal);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).to.have.been.calledOnce;
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: listSignal.id,
        providerEndpoint: 'NameService',
        providerMethod: 'nameListSignal',
        params: undefined,
        parentClientSignalId: undefined,
      });
    });

    it('should be able to set value internally', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', value: 'Bob' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(2);
    });

    it('should throw an error when trying to set value externally', () => {
      expect(() => {
        // @ts-expect-error suppress TS error to fail at runtime for testing purposes
        listSignal.value = ['Alice', 'Bob'];
      }).to.throw('Value of the collection signals cannot be set.');
    });

    it('should send the correct event when insertLast is called', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;

      listSignal.insertLast('Alice');
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: listSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params?.event.id, type: 'insert', position: 'last', value: 'Alice', accepted: false },
      });
    });

    it('should validate that entryId is defined when insertLast event is accepted', () => {
      subscribeToSignalViaEffect(listSignal);
      const acceptedEventWithoutEntryId: InsertLastStateEvent<string> = {
        id: 'some-id',
        type: 'insert',
        position: 'last',
        value: 'Alice',
        accepted: true,
      };
      expect(() => {
        simulateReceivingEvent(acceptedEventWithoutEntryId);
      }).to.throw('Unexpected state: Entry id should be defined when insert last event is accepted');
    });

    it('should update the value when the accepted update for insertLast is received', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const acceptedEvent1: InsertLastStateEvent<string> = {
        id: 'some-id',
        type: 'insert',
        position: 'last',
        value: 'Alice',
        entryId: 'some-entry-id-1',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent1);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[0].id).to.equal('some-entry-id-1');

      const acceptedEvent2: InsertLastStateEvent<string> = {
        id: 'some-id',
        type: 'insert',
        position: 'last',
        value: 'Bob',
        entryId: 'some-entry-id-2',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent2);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[1].value).to.equal('Bob');
      expect(listSignal.value[1].id).to.equal('some-entry-id-2');
    });

    it('should send the correct event when remove is called', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', value: 'Bob' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(2);
      const firstElement = listSignal.value.values().next().value!;
      listSignal.remove(firstElement);
      const [, , params] = client.call.firstCall.args;

      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledOnce;
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledWithMatch('SignalsHandler', 'update', {
        clientSignalId: listSignal.id,
        // @ts-expect-error params.event type has id property
        event: { id: params?.event.id, type: 'remove', entryId: firstElement.id, accepted: false },
      });
    });

    it('should do nothing when the update for removing a non-existing entry is received', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const acceptedEvent: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: 'non-existing-entry-id',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent);
      expect(listSignal.value).to.be.empty;

      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', value: 'Bob' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(2);

      simulateReceivingEvent(acceptedEvent);
      expect(listSignal.value).to.have.length(2);
    });

    it('should update the value correctly when the accepted remove event is removing the head', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', value: 'John' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(3);

      const acceptedEvent1: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '1',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent1);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Bob');
      expect(listSignal.value[1].value).to.equal('John');

      const acceptedEvent2: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '2',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent2);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('John');

      const acceptedEvent3: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '3',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent3);
      expect(listSignal.value).to.be.empty;
    });

    it('should update the value correctly when the accepted remove event is removing the tail', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', value: 'John' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(3);

      const acceptedEvent1: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '3',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent1);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');

      const acceptedEvent2: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '2',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent2);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');

      const acceptedEvent3: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '1',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent3);
      expect(listSignal.value).to.be.empty;
    });

    it('should update the value correctly when the accepted remove event is removing the middle element', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', next: '4', value: 'John' },
          { id: '4', prev: '3', value: 'Jane' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(4);

      const acceptedEvent1: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '2',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent1);
      expect(listSignal.value).to.have.length(3);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('John');
      expect(listSignal.value[2].value).to.equal('Jane');

      const acceptedEvent2: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '3',
        accepted: true,
      };
      simulateReceivingEvent(acceptedEvent2);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Jane');
    });

    it('should do nothing when receiving a rejected event', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const notAcceptedEvent1: InsertLastStateEvent<string> = {
        id: 'some-id',
        type: 'insert',
        position: 'last',
        value: 'Bob',
        entryId: 'some-entry-id-2',
        accepted: false,
      };
      simulateReceivingEvent(notAcceptedEvent1);
      expect(listSignal.value).to.be.empty;

      const notAcceptedSnapshot = {
        id: '123',
        type: 'snapshot',
        accepted: false,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', next: '4', value: 'John' },
          { id: '4', prev: '3', value: 'Jane' },
        ],
      };
      simulateReceivingEvent(notAcceptedSnapshot);
      expect(listSignal.value).to.be.empty;

      const acceptedSnapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', next: '4', value: 'John' },
          { id: '4', prev: '3', value: 'Jane' },
        ],
      };
      simulateReceivingEvent(acceptedSnapshot);
      expect(listSignal.value).to.have.length(4);

      const notAcceptedEvent2: RemoveStateEvent = {
        id: 'some-id',
        type: 'remove',
        value: undefined as never,
        entryId: '2',
        accepted: false,
      };
      simulateReceivingEvent(notAcceptedEvent2);
      expect(listSignal.value).to.have.length(4);
    });

    it('should do nothing when a non existent signal is passed to remove function', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [
          { id: '1', next: '2', value: 'Alice' },
          { id: '2', prev: '1', next: '3', value: 'Bob' },
          { id: '3', prev: '2', next: '4', value: 'John' },
          { id: '4', prev: '3', value: 'Jane' },
        ],
      };
      simulateReceivingEvent(snapshot);
      expect(listSignal.value).to.have.length(4);

      const nonExistentSignal = new ValueSignal<string>('', {
        client,
        endpoint: 'NameService',
        method: 'nameListSignal',
      });
      listSignal.remove(nonExistentSignal);
      expect(listSignal.value).to.have.length(4);
    });

    it('should resolve the result promise after insertLast', (done) => {
      subscribeToSignalViaEffect(listSignal);
      listSignal.insertLast('Alice').result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      const insertEvent: InsertLastStateEvent<string> = {
        id: (params!.event as { id: string }).id,
        type: 'insert',
        position: 'last',
        value: 'Alice',
        entryId: '1',
        accepted: true,
      };
      simulateReceivingEvent(insertEvent);
    });

    it('should resolve the result promise after remove', (done) => {
      subscribeToSignalViaEffect(listSignal);
      const snapshot = {
        id: '123',
        type: 'snapshot',
        accepted: true,
        value: undefined,
        entries: [{ id: '1', value: 'Alice' }],
      };
      simulateReceivingEvent(snapshot);
      const firstElement = listSignal.value.values().next().value!;
      listSignal.remove(firstElement).result.then(done, () => done('Should not reject'));
      const [, , params] = client.call.firstCall.args;
      const removeEvent: RemoveStateEvent = {
        id: (params!.event as { id: string }).id,
        type: 'remove',
        value: undefined as never,
        entryId: '1',
        accepted: true,
      };
      simulateReceivingEvent(removeEvent);
    });

    it('should resolve the result promise after removing a non-existing entry', (done) => {
      subscribeToSignalViaEffect(listSignal);
      const nonExistentSignal = new ValueSignal<string>('', {
        client,
        endpoint: 'NameService',
        method: 'nameListSignal',
      });
      listSignal.remove(nonExistentSignal).result.then(done, () => done('Should not reject'));
    });
  });
});
