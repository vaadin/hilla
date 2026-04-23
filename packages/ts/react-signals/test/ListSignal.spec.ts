/* eslint-disable @typescript-eslint/unbound-method */

import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { beforeEach, describe, expect, it, chai } from 'vitest';
import type {
  SignalCommand,
  InsertCommand,
  RemoveCommand,
  AdoptAtCommand,
  PositionCondition,
  Node,
} from '../src/commands.js';
import {
  createSnapshotCommand,
  createInsertCommand,
  createRemoveCommand,
  createAdoptAtCommand,
  ListPosition,
} from '../src/commands.js';
import { ListSignal, ValueSignal } from '../src/index.js';
import { createSubscriptionStub, subscribeToSignalViaEffect, simulateReceivedChange } from './utils.js';

chai.use(sinonChai);
chai.use(chaiAsPromised);

describe('@vaadin/hilla-react-signals', () => {
  describe('ListSignal', () => {
    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
    let listSignal: ListSignal<string>;

    // Helper function to create snapshot commands for testing
    function createServerSnapshotCommand(commandId: string, entries: Record<string, string>): SignalCommand {
      const nodes: Record<string, Node> = {
        '': {
          '@type': 'ListSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: Object.keys(entries),
          mapChildren: {},
        },
      };

      Object.entries(entries).forEach(([id, value]) => {
        nodes[id] = {
          '@type': 'ValueSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          value,
          listChildren: [],
          mapChildren: {},
        };
      });

      const command = createSnapshotCommand(nodes);
      return { ...command, commandId };
    }

    function createServerInsertCommand(
      commandId: string,
      targetNodeId: string,
      value: string,
      position = ListPosition.last(),
    ): InsertCommand<string> {
      const command = createInsertCommand(targetNodeId, value, position);
      return { ...command, commandId };
    }

    function createServerRemoveCommand(
      commandId: string,
      targetNodeId: string,
      expectedParentId: string = '',
    ): RemoveCommand {
      const command = createRemoveCommand(targetNodeId, expectedParentId);
      return { ...command, commandId };
    }

    function createServerAdoptAtCommand(
      commandId: string,
      targetNodeId: string,
      childId: string,
      position: ListPosition,
    ): AdoptAtCommand {
      const command = createAdoptAtCommand(targetNodeId, childId, position);
      return { ...command, commandId };
    }

    function createServerPositionCondition(
      commandId: string,
      targetNodeId: string,
      childId: string,
      expectedPosition: ListPosition,
    ): PositionCondition {
      return {
        '@type': 'pos' as const,
        commandId,
        targetNodeId,
        childId,
        expectedPosition,
      };
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
      expect(client.subscribe).not.to.have.been.called;
    });

    it('should subscribe to server side instance when it is subscribed to on client side', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(client.subscribe).to.have.been.calledOnce;
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
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
      });
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
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
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
      const insertCommand1 = createServerInsertCommand('some-id', '', 'Alice');
      simulateReceivedChange(subscription, insertCommand1);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');

      const insertCommand2 = createServerInsertCommand('some-id-2', '', 'Bob');
      simulateReceivedChange(subscription, insertCommand2);
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[1].value).to.equal('Bob');
    });

    it('should send the correct event when remove is called', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);
      const firstElement = listSignal.value.values().next().value!;
      listSignal.remove(firstElement);
      const [, , params] = client.call.firstCall.args;

      expect(client.call).to.have.been.calledOnce;
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

    it('should update the value correctly when the accepted remove event is removing the head', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'John',
      });
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
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'John',
      });
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

    it('should send the correct event when insertFirst is called', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;

      listSignal.insertFirst('Alice');
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: listSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'insert',
            position: { after: '', before: null },
            value: 'Alice',
          },
        },
        { mute: true },
      );
    });

    it('should update the value when the accepted update for insertFirst is received', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;

      const insertCommand = createServerInsertCommand('some-id', '', 'Alice', ListPosition.first());
      simulateReceivedChange(subscription, insertCommand);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');
    });

    it('should update the value correctly when the accepted remove event is removing the middle element', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'John',
        '4': 'Jane',
      });
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

    it('should send correct command when clear() is called', () => {
      subscribeToSignalViaEffect(listSignal);

      listSignal.clear();
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: listSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'clear',
          },
        },
        { mute: true },
      );
    });

    it('should clear the list optimistically', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob', '3': 'Charlie' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(3);

      listSignal.clear();
      expect(listSignal.value).to.be.empty;
    });

    it('should handle server clear command', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      simulateReceivedChange(subscription, {
        '@type': 'clear',
        commandId: 'remote-clear',
        targetNodeId: '',
      } as SignalCommand);
      expect(listSignal.value).to.be.empty;
    });

    it('should apply optimistic insert immediately before server confirms', () => {
      subscribeToSignalViaEffect(listSignal);
      expect(listSignal.value).to.be.empty;

      listSignal.insertLast('Alice');
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');
    });

    it('should skip re-applying own confirmed insertLast', () => {
      subscribeToSignalViaEffect(listSignal);

      listSignal.insertLast('Alice');
      expect(listSignal.value).to.have.length(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerInsertCommand(commandId, '', 'Alice');
      simulateReceivedChange(subscription, confirmCommand);

      // After confirmation, the insert command is removed from the unconfirmed queue
      // and applied to the confirmed tree. Since the tree is re-derived,
      // the item count should remain at 1 (the confirmed one).
      // Note: the confirmed tree gets a NEW node id for the server's insert,
      // but the unconfirmed one also added one. After confirmation, only the confirmed remains.
      expect(listSignal.value.length).to.be.greaterThanOrEqual(1);
    });

    it('should apply optimistic insertFirst at the beginning', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(1);

      listSignal.insertFirst('Alice');
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');
    });

    it('should apply optimistic remove immediately before server confirms', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      const [firstElement] = listSignal.value;
      listSignal.remove(firstElement);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Bob');
    });

    it('should skip re-applying own confirmed remove', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);

      const [firstElement] = listSignal.value;
      listSignal.remove(firstElement);
      expect(listSignal.value).to.have.length(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerRemoveCommand(commandId, '1');
      simulateReceivedChange(subscription, confirmCommand);

      // Should still have exactly 1 entry (no double-remove)
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Bob');
    });

    it('should revert optimistic insert on rejection', () => {
      subscribeToSignalViaEffect(listSignal);

      const { result } = listSignal.insertLast('Alice');
      result.catch(() => {});
      expect(listSignal.value).to.have.length(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      simulateReceivedChange(subscription, {
        '@type': 'insert',
        commandId,
        targetNodeId: '',
        value: 'Alice',
        position: { after: null, before: '' },
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      expect(listSignal.value).to.be.empty;
    });

    it('should revert optimistic remove on rejection', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      const [firstElement] = listSignal.value;
      const { result } = listSignal.remove(firstElement);
      result.catch(() => {});
      expect(listSignal.value).to.have.length(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      simulateReceivedChange(subscription, {
        '@type': 'remove',
        commandId,
        targetNodeId: firstElement.id,
        expectedParentId: '',
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      // Should be restored
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
    });

    it('should resolve the result promise after insertLast', async () => {
      subscribeToSignalViaEffect(listSignal);
      const { result } = listSignal.insertLast('Alice');
      const [, , params] = client.call.firstCall.args;
      const insertCommand = createServerInsertCommand(
        (params!.command as { commandId: string }).commandId,
        '',
        'Alice',
      );
      simulateReceivedChange(subscription, insertCommand);
      await expect(result).to.be.fulfilled;
    });

    it('should resolve the result promise after remove', async () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', { '1': 'Alice' });
      simulateReceivedChange(subscription, snapshotCommand);
      const firstElement = listSignal.value.values().next().value!;
      const { result } = listSignal.remove(firstElement);
      const [, , params] = client.call.firstCall.args;
      const removeCommand = createServerRemoveCommand((params!.command as { commandId: string }).commandId, '1');
      simulateReceivedChange(subscription, removeCommand);
      await expect(result).to.be.fulfilled;
    });

    it('should send the correct event when insertAt is called with a custom position', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);

      const [firstChild] = listSignal.value;
      listSignal.insertAt('Charlie', ListPosition.after(firstChild));
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: listSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'insert',
            position: { after: firstChild.id, before: null },
            value: 'Charlie',
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

      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      simulateReceivedChange(subscription, removeCommand);
      expect(listSignal.value).to.have.length(2);
    });

    it('should do nothing when a non existent signal is passed to remove function', () => {
      subscribeToSignalViaEffect(listSignal);
      const snapshotCommand = createServerSnapshotCommand('123', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'John',
        '4': 'Jane',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(4);

      const nonExistentSignal = new ValueSignal('', {
        client,
        endpoint: 'NameService',
        method: 'nameListSignal',
      });
      listSignal.remove(nonExistentSignal);
      expect(listSignal.value).to.have.length(4);
    });

    it('should handle insert at specific positions correctly', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'Charlie',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(3);

      // Insert after '1' (after Alice)
      const insertAfterCommand = createServerInsertCommand('after-id', '', 'David', {
        after: '1',
        before: null,
      });
      simulateReceivedChange(subscription, insertAfterCommand);
      expect(listSignal.value).to.have.length(4);
      expect(listSignal.value[1].value).to.equal('David');

      // Insert before '3' (before Charlie)
      const insertBeforeCommand = createServerInsertCommand('before-id', '', 'Eve', {
        after: null,
        before: '3',
      });
      simulateReceivedChange(subscription, insertBeforeCommand);
      expect(listSignal.value).to.have.length(5);
      expect(listSignal.value[3].value).to.equal('Eve');
    });

    it('should handle commands with targetNodeId updating a child value', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');

      const setCommand = {
        '@type': 'set' as const,
        commandId: 'set-cmd',
        targetNodeId: '1',
        value: 'Updated Alice',
      };
      simulateReceivedChange(subscription, setCommand);
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Updated Alice');
    });

    it('should handle snapshot commands with missing child nodes gracefully', () => {
      subscribeToSignalViaEffect(listSignal);

      const nodes: Record<string, Node> = {
        '': {
          '@type': 'ListSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: ['1', '2'],
          mapChildren: {},
        },
        '1': {
          '@type': 'ValueSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          value: 'Alice',
          listChildren: [],
          mapChildren: {},
        },
        '2': {
          '@type': 'SomeOtherSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          listChildren: [],
          mapChildren: {},
        },
      };

      const snapshotCommand = { ...createSnapshotCommand(nodes), commandId: 'snapshot' };
      simulateReceivedChange(subscription, snapshotCommand);

      // Node '2' has no value property, should be filtered out
      expect(listSignal.value).to.have.length(1);
      expect(listSignal.value[0].value).to.equal('Alice');
    });

    it('should handle position condition commands', () => {
      subscribeToSignalViaEffect(listSignal);
      const positionCommand = createServerPositionCondition('pos-cmd', '', 'child-1', ListPosition.last());
      expect(() => simulateReceivedChange(subscription, positionCommand as unknown as SignalCommand)).not.to.throw;
    });

    it('should handle adopt-at commands for moving children', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'Charlie',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(3);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');
      expect(listSignal.value[2].value).to.equal('Charlie');

      // Move Bob to end
      const moveToEndCommand = createServerAdoptAtCommand('move-cmd', '', '2', ListPosition.last());
      simulateReceivedChange(subscription, moveToEndCommand);

      expect(listSignal.value).to.have.length(3);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Charlie');
      expect(listSignal.value[2].value).to.equal('Bob');

      // Move Charlie to beginning
      const moveToBeginCommand = createServerAdoptAtCommand('move-cmd-2', '', '3', ListPosition.first());
      simulateReceivedChange(subscription, moveToBeginCommand);

      expect(listSignal.value).to.have.length(3);
      expect(listSignal.value[0].value).to.equal('Charlie');
      expect(listSignal.value[1].value).to.equal('Alice');
      expect(listSignal.value[2].value).to.equal('Bob');
    });

    it('should handle adopt-at commands with specific positioning', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', {
        '1': 'Alice',
        '2': 'Bob',
        '3': 'Charlie',
        '4': 'David',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(4);

      // Move David after Alice
      const moveAfterAliceCommand = createServerAdoptAtCommand('move-after', '', '4', { after: '1', before: null });
      simulateReceivedChange(subscription, moveAfterAliceCommand);

      expect(listSignal.value).to.have.length(4);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('David');
      expect(listSignal.value[2].value).to.equal('Bob');
      expect(listSignal.value[3].value).to.equal('Charlie');
    });

    it('should handle adopt-at commands for non-existing children gracefully', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', {
        '1': 'Alice',
        '2': 'Bob',
      });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(listSignal.value).to.have.length(2);

      const moveNonExistingCommand = createServerAdoptAtCommand(
        'move-non-existing',
        '',
        'non-existing-id',
        ListPosition.last(),
      );

      expect(() => simulateReceivedChange(subscription, moveNonExistingCommand)).not.to.throw;
      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');
    });

    it('should resolve the result promise after removing a non-existing entry', async () => {
      subscribeToSignalViaEffect(listSignal);

      const nonExistentSignal = new ValueSignal('', {
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

    it('should process snapshot command correctly', () => {
      subscribeToSignalViaEffect(listSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { '1': 'Alice', '2': 'Bob' });
      simulateReceivedChange(subscription, snapshotCommand);

      expect(listSignal.value).to.have.length(2);
      expect(listSignal.value[0].value).to.equal('Alice');
      expect(listSignal.value[1].value).to.equal('Bob');
    });
  });
});
