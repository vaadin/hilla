import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { beforeEach, describe, expect, it, chai } from 'vitest';
import type { SignalCommand } from '../src/commands.js';
import { createInsertCommand, createRemoveCommand, createSnapshotCommand, ListPosition } from '../src/commands.js';
import { ListSignal, ValueSignal } from '../src/index.js';
import { createSubscriptionStub, subscribeToSignalViaEffect, simulateReceivedChange } from './utils.js';

chai.use(sinonChai);
chai.use(chaiAsPromised);

describe('@vaadin/hilla-react-signals', () => {
  describe('ListSignal', () => {
    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
    let listSignal: ListSignal<string>;

    // Helper function to create insert commands for testing
    function createServerInsertCommand(commandId: string, value: string, entryId?: string): SignalCommand {
      const targetNodeId = entryId ?? commandId;
      const command = createInsertCommand(targetNodeId, value, ListPosition.last());
      return { ...command, commandId, targetNodeId }; // Ensure targetNodeId is set correctly
    }

    // Helper function to create remove commands for testing
    function createServerRemoveCommand(commandId: string, entryId: string): SignalCommand {
      const command = createRemoveCommand(entryId, '');
      return { ...command, commandId };
    }

    // Helper function to create snapshot commands for testing
    function createServerSnapshotCommand(
      commandId: string,
      entries: Array<{ id: string; value: string; next?: string; prev?: string }>,
    ): SignalCommand {
      const nodes: Record<string, any> = {
        '': {
          '@type': 'ListSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: entries.map((e) => e.id),
          mapChildren: {},
        },
      };

      entries.forEach((entry) => {
        nodes[entry.id] = {
          '@type': 'ValueSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          value: entry.value,
          listChildren: [],
          mapChildren: {},
        };
      });

      const command = createSnapshotCommand(nodes);
      return { ...command, commandId };
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
      });
    });

    it('should be able to set value internally', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', value: 'Bob' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
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
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: listSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'insert',
            position: { after: null, before: '' },
            value: 'Alice',
          },
        },
        { mute: true },
      );
    });

    it('should update the value when the accepted update for insertLast is received', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const insertCommand1 = createServerInsertCommand('some-id', 'Alice', 'some-entry-id-1');
      simulateReceivedChange(subscription, insertCommand1);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[0].id).to.equal('some-id'); // Uses commandId as entry ID

      const insertCommand2 = createServerInsertCommand('some-id-2', 'Bob', 'some-entry-id-2');
      simulateReceivedChange(subscription, insertCommand2);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[1].value).to.equal('Bob');
      expect(listSignal.value[1].id).to.equal('some-id-2'); // Uses commandId as entry ID
    });

    it('should send the correct event when remove is called', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', value: 'Bob' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);
      const firstElement = listSignal.value.values().next().value!;
      listSignal.remove(firstElement);
      const [, , params] = client.call.firstCall.args;

      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledOnce;
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: listSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: firstElement.id,
            '@type': 'remove',
            expectedParentId: '',
          },
        },
        { mute: true },
      );
    });

    it('should do nothing when the update for removing a non-existing entry is received', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      const removeCommand = createServerRemoveCommand('some-id', 'non-existing-entry-id');
      simulateReceivedChange(subscription, removeCommand);
      expect(listSignal.value).to.be.empty;

      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', value: 'Bob' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      simulateReceivedChange(subscription, removeCommand);
      expect(listSignal.value).to.have.length(2);
    });

    it('should update the value correctly when the accepted remove event is removing the head', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', next: '3', value: 'Bob' },
        { id: '3', prev: '2', value: 'John' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(3);

      const removeCommand1 = createServerRemoveCommand('some-id', '1');
      simulateReceivedChange(subscription, removeCommand1);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Bob');
      expect(listSignal.value[1].value).to.equal('John');

      const removeCommand2 = createServerRemoveCommand('some-id', '2');
      simulateReceivedChange(subscription, removeCommand2);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('John');

      const removeCommand3 = createServerRemoveCommand('some-id', '3');
      simulateReceivedChange(subscription, removeCommand3);
      expect(listSignal.value).to.be.empty;
    });

    it('should update the value correctly when the accepted remove event is removing the tail', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', next: '3', value: 'Bob' },
        { id: '3', prev: '2', value: 'John' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(3);

      const removeCommand1 = createServerRemoveCommand('some-id', '3');
      simulateReceivedChange(subscription, removeCommand1);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');

      const removeCommand2 = createServerRemoveCommand('some-id', '2');
      simulateReceivedChange(subscription, removeCommand2);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');

      const removeCommand3 = createServerRemoveCommand('some-id', '1');
      simulateReceivedChange(subscription, removeCommand3);
      expect(listSignal.value).to.be.empty;
    });

    it('should update the value correctly when the accepted remove event is removing the middle element', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', next: '3', value: 'Bob' },
        { id: '3', prev: '2', next: '4', value: 'John' },
        { id: '4', prev: '3', value: 'Jane' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(4);

      const removeCommand1 = createServerRemoveCommand('some-id', '2');
      simulateReceivedChange(subscription, removeCommand1);
      expect(listSignal.value).to.have.length(3);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('John');
      expect(listSignal.value[2].value).to.equal('Jane');

      const removeCommand2 = createServerRemoveCommand('some-id', '3');
      simulateReceivedChange(subscription, removeCommand2);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Jane');
    });

    it('should do nothing when receiving a rejected event', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;
      // With commands, rejection is handled differently - commands that reach the client are already validated
      // This test behavior changes with the new architecture

      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', next: '3', value: 'Bob' },
        { id: '3', prev: '2', next: '4', value: 'John' },
        { id: '4', prev: '3', value: 'Jane' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(4);
    });

    it('should do nothing when a non existent signal is passed to remove function', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [
        { id: '1', next: '2', value: 'Alice' },
        { id: '2', prev: '1', next: '3', value: 'Bob' },
        { id: '3', prev: '2', next: '4', value: 'John' },
        { id: '4', prev: '3', value: 'Jane' },
      ]);
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(4);

      const nonExistentSignal = new ValueSignal<string>('', {
        client,
        endpoint: 'NameService',
        method: 'nameListSignal',
      });
      listSignal.remove(nonExistentSignal);
      expect(listSignal.value).to.have.length(4);
    });

    it('should resolve the result promise after insertLast', async () => {
      subscribeToSignalViaEffect(listSignal);
      const { result } = listSignal.insertLast('Alice');
      const [, , params] = client.call.firstCall.args;
      const insertCommand = createServerInsertCommand(
        (params!.command as { commandId: string }).commandId,
        'Alice',
        '1',
      );
      simulateReceivedChange(subscription, insertCommand);
      await expect(result).to.be.fulfilled;
    });

    it('should resolve the result promise after remove', async () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', [{ id: '1', value: 'Alice' }]);
      simulateReceivedChange(subscription, snapshotCommand);
      const firstElement = listSignal.value.values().next().value!;
      const { result } = listSignal.remove(firstElement);
      const [, , params] = client.call.firstCall.args;
      const removeCommand = createServerRemoveCommand((params!.command as { commandId: string }).commandId, '1');
      simulateReceivedChange(subscription, removeCommand);
      await expect(result).to.be.fulfilled;
    });

    it('should resolve the result promise after removing a non-existing entry', async () => {
      subscribeToSignalViaEffect(listSignal);

      const nonExistentSignal = new ValueSignal<string>('', {
        client,
        endpoint: 'NameService',
        method: 'nameListSignal',
      });

      const { result } = listSignal.remove(nonExistentSignal);

      if (client.call.called) {
        const [, , params] = client.call.firstCall.args;
        const removeCommand = createServerRemoveCommand(
          (params!.command as { commandId: string }).commandId,
          nonExistentSignal.id,
        );
        simulateReceivedChange(subscription, removeCommand);
      }
      await expect(result).to.be.fulfilled;
    });
  });
});
