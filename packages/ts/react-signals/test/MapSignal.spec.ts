/* eslint-disable @typescript-eslint/unbound-method */

import { ConnectClient, type Subscription } from '@vaadin/hilla-frontend';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { beforeEach, describe, expect, it, chai } from 'vitest';
import type { SignalCommand, PutCommand, RemoveByKeyCommand, Node } from '../src/commands.js';
import { createSnapshotCommand, createPutCommand, createRemoveByKeyCommand, ZERO } from '../src/commands.js';
import { MapSignal, ValueSignal } from '../src/index.js';
import { createSubscriptionStub, subscribeToSignalViaEffect, simulateReceivedChange } from './utils.js';

chai.use(sinonChai);
chai.use(chaiAsPromised);

describe('@vaadin/hilla-react-signals', () => {
  describe('MapSignal', () => {
    let client: sinon.SinonStubbedInstance<ConnectClient>;
    let subscription: sinon.SinonSpiedInstance<Subscription<SignalCommand>>;
    let mapSignal: MapSignal<string>;

    function createServerSnapshotCommand(commandId: string, entries: Record<string, string>): SignalCommand {
      const mapChildren: Record<string, string> = {};
      const nodes: Record<string, Node> = {
        '': {
          '@type': 'MapSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: [],
          mapChildren,
        },
      };

      Object.entries(entries).forEach(([key, value], index) => {
        const childId = `child-${index}`;
        mapChildren[key] = childId;
        nodes[childId] = {
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

    function createServerPutCommand(commandId: string, key: string, value: string): PutCommand<string> {
      const command = createPutCommand(ZERO, key, value);
      return { ...command, commandId };
    }

    function createServerRemoveByKeyCommand(commandId: string, key: string): RemoveByKeyCommand {
      const command = createRemoveByKeyCommand(ZERO, key);
      return { ...command, commandId };
    }

    beforeEach(() => {
      client = sinon.createStubInstance(ConnectClient);
      client.call.resolves();
      subscription = createSubscriptionStub();
      client.subscribe.returns(subscription);
      mapSignal = new MapSignal({ client, endpoint: 'TestService', method: 'testMapSignal' });
    });

    it('should create a new MapSignal instance', () => {
      expect(mapSignal).to.be.an.instanceOf(MapSignal);
    });

    it('should have empty map by default', () => {
      expect(mapSignal.value).to.be.an.instanceOf(Map);
      expect(mapSignal.value.size).to.equal(0);
    });

    it('should subscribe to server when subscribed to on client side', () => {
      subscribeToSignalViaEffect(mapSignal);
      expect(client.subscribe).to.have.been.calledOnce;
      expect(client.subscribe).to.have.been.calledWith('SignalsHandler', 'subscribe', {
        clientSignalId: mapSignal.id,
        providerEndpoint: 'TestService',
        providerMethod: 'testMapSignal',
        params: undefined,
      });
    });

    it('should send correct command when put() is called', () => {
      subscribeToSignalViaEffect(mapSignal);

      mapSignal.put('name', 'Alice');
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: mapSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'put',
            key: 'name',
            value: 'Alice',
          },
        },
        { mute: true },
      );
    });

    it('should apply optimistic update immediately when put() is called', () => {
      subscribeToSignalViaEffect(mapSignal);
      expect(mapSignal.value.size).to.equal(0);

      mapSignal.put('name', 'Alice');
      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.get('name')).to.be.an.instanceOf(ValueSignal);
      expect(mapSignal.value.get('name')!.value).to.equal('Alice');
    });

    it('should skip re-applying own confirmed put', () => {
      subscribeToSignalViaEffect(mapSignal);

      mapSignal.put('name', 'Alice');
      expect(mapSignal.value.size).to.equal(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerPutCommand(commandId, 'name', 'Alice');
      simulateReceivedChange(subscription, confirmCommand);

      // After confirmation the map should still have the entry
      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.get('name')!.value).to.equal('Alice');
    });

    it('should apply remote put from another client', () => {
      subscribeToSignalViaEffect(mapSignal);
      expect(mapSignal.value.size).to.equal(0);

      const remotePut = createServerPutCommand('remote-id', 'name', 'Bob');
      simulateReceivedChange(subscription, remotePut);

      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.get('name')!.value).to.equal('Bob');
    });

    it('should send correct command when removeKey() is called', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(1);

      mapSignal.removeKey('name');
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: mapSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'removeKey',
            key: 'name',
          },
        },
        { mute: true },
      );
    });

    it('should apply optimistic remove immediately when removeKey() is called', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice', age: '30' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(2);

      mapSignal.removeKey('name');
      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.has('name')).to.be.false;
      expect(mapSignal.value.has('age')).to.be.true;
    });

    it('should skip re-applying own confirmed removeKey', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice', age: '30' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(2);

      mapSignal.removeKey('name');
      expect(mapSignal.value.size).to.equal(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerRemoveByKeyCommand(commandId, 'name');
      simulateReceivedChange(subscription, confirmCommand);

      // Should still have exactly 1 entry (no re-remove)
      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.has('age')).to.be.true;
    });

    it('should apply remote removeKey from another client', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice', age: '30' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(2);

      const remoteRemove = createServerRemoveByKeyCommand('remote-remove', 'name');
      simulateReceivedChange(subscription, remoteRemove);

      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.has('name')).to.be.false;
      expect(mapSignal.value.has('age')).to.be.true;
    });

    it('should send correct command when clear() is called', () => {
      subscribeToSignalViaEffect(mapSignal);

      mapSignal.clear();
      expect(client.call).to.have.been.calledOnce;

      const [, , params] = client.call.firstCall.args;
      expect(client.call).to.have.been.calledWithMatch(
        'SignalsHandler',
        'update',
        {
          clientSignalId: mapSignal.id,
          command: {
            commandId: (params?.command as { commandId: string }).commandId,
            targetNodeId: '',
            '@type': 'clear',
          },
        },
        { mute: true },
      );
    });

    it('should clear the map optimistically', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { a: '1', b: '2', c: '3' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(3);

      mapSignal.clear();
      expect(mapSignal.value.size).to.equal(0);
    });

    it('should process snapshot command correctly', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice', city: 'NYC' });
      simulateReceivedChange(subscription, snapshotCommand);

      expect(mapSignal.value.size).to.equal(2);
      expect(mapSignal.value.get('name')!.value).to.equal('Alice');
      expect(mapSignal.value.get('city')!.value).to.equal('NYC');
    });

    it('should revert optimistic put on rejection', () => {
      subscribeToSignalViaEffect(mapSignal);

      const { result } = mapSignal.put('name', 'Alice');
      // Catch to prevent unhandled rejection
      result.catch(() => {});
      expect(mapSignal.value.size).to.equal(1);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      // Simulate rejection
      simulateReceivedChange(subscription, {
        '@type': 'put',
        commandId,
        targetNodeId: '',
        key: 'name',
        value: 'Alice',
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      expect(mapSignal.value.size).to.equal(0);
    });

    it('should revert optimistic removeKey on rejection', () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice' });
      simulateReceivedChange(subscription, snapshotCommand);
      expect(mapSignal.value.size).to.equal(1);

      const { result } = mapSignal.removeKey('name');
      // Catch to prevent unhandled rejection
      result.catch(() => {});
      expect(mapSignal.value.size).to.equal(0);

      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };

      // Simulate rejection — entry should be restored
      simulateReceivedChange(subscription, {
        '@type': 'removeKey',
        commandId,
        targetNodeId: '',
        key: 'name',
        accepted: false,
        reason: 'conflict',
      } as unknown as SignalCommand);

      expect(mapSignal.value.size).to.equal(1);
      expect(mapSignal.value.has('name')).to.be.true;
    });

    it('should resolve result promise after put is confirmed', async () => {
      subscribeToSignalViaEffect(mapSignal);

      const { result } = mapSignal.put('name', 'Alice');
      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerPutCommand(commandId, 'name', 'Alice');
      simulateReceivedChange(subscription, confirmCommand);

      await expect(result).to.be.fulfilled;
    });

    it('should resolve result promise after removeKey is confirmed', async () => {
      subscribeToSignalViaEffect(mapSignal);

      const snapshotCommand = createServerSnapshotCommand('snapshot', { name: 'Alice' });
      simulateReceivedChange(subscription, snapshotCommand);

      const { result } = mapSignal.removeKey('name');
      const [, , params] = client.call.firstCall.args;
      const { commandId } = params!.command as { commandId: string };
      const confirmCommand = createServerRemoveByKeyCommand(commandId, 'name');
      simulateReceivedChange(subscription, confirmCommand);

      await expect(result).to.be.fulfilled;
    });
  });
});
