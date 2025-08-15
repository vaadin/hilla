import { describe, expect, it } from 'vitest';
import {
  createAdoptAtCommand,
  createIncrementCommand,
  createInsertCommand,
  createPositionCondition,
  createRemoveCommand,
  createSetCommand,
  createSnapshotCommand,
  createTransactionCommand,
  createValueCondition,
  EDGE,
  isAdoptAtCommand,
  isIncrementCommand,
  isInsertCommand,
  isPositionCondition,
  isRemoveCommand,
  isSetCommand,
  isSnapshotCommand,
  isTransactionCommand,
  isValueCondition,
  ListPosition,
  type Node,
  ZERO,
} from '../src/commands.js';

describe('@vaadin/hilla-react-signals', () => {
  describe('CreateCommandType', () => {
    it('should create correct SetCommand', () => {
      const setCommand1 = createSetCommand('target1', 'foo');
      expect(setCommand1.commandId).to.be.not.null;
      expect(setCommand1['@type']).to.equal('set');
      expect(setCommand1.value).to.equal('foo');
      expect(setCommand1.targetNodeId).to.equal('target1');
      expect(isSetCommand(setCommand1)).to.be.true;

      const setCommand2 = createSetCommand('target2', 42);
      expect(setCommand2.commandId).to.be.not.null;
      expect(setCommand2['@type']).to.equal('set');
      expect(setCommand2.value).to.equal(42);
      expect(setCommand2.targetNodeId).to.equal('target2');
      expect(isSetCommand(setCommand2)).to.be.true;

      const setCommand3 = createSetCommand('target3', { nested: 'object' });
      expect(setCommand3.commandId).to.be.not.null;
      expect(setCommand3['@type']).to.equal('set');
      expect(setCommand3.value).to.deep.equal({ nested: 'object' });
      expect(setCommand3.targetNodeId).to.equal('target3');
      expect(isSetCommand(setCommand3)).to.be.true;
    });

    it('should create correct ValueCondition', () => {
      const valueCondition1 = createValueCondition('target1', 'expected');
      expect(valueCondition1.commandId).to.be.not.null;
      expect(valueCondition1['@type']).to.equal('value');
      expect(valueCondition1.expectedValue).to.equal('expected');
      expect(valueCondition1.targetNodeId).to.equal('target1');
      expect(isValueCondition(valueCondition1)).to.be.true;
      expect(isSetCommand(valueCondition1)).to.be.false;

      const valueCondition2 = createValueCondition('target2', 123);
      expect(valueCondition2.commandId).to.be.not.null;
      expect(valueCondition2['@type']).to.equal('value');
      expect(valueCondition2.expectedValue).to.equal(123);
      expect(valueCondition2.targetNodeId).to.equal('target2');
      expect(isValueCondition(valueCondition2)).to.be.true;
      expect(isSetCommand(valueCondition2)).to.be.false;
    });

    it('should create correct IncrementCommand', () => {
      const incrementCommand1 = createIncrementCommand('target1', 42);
      expect(incrementCommand1.commandId).to.be.not.null;
      expect(incrementCommand1['@type']).to.equal('inc');
      expect(incrementCommand1.delta).to.equal(42);
      expect(incrementCommand1.targetNodeId).to.equal('target1');
      expect(isIncrementCommand(incrementCommand1)).to.be.true;
      expect(isSetCommand(incrementCommand1)).to.be.false;
      expect(isValueCondition(incrementCommand1)).to.be.false;

      const incrementCommand2 = createIncrementCommand('target2', -10);
      expect(incrementCommand2.commandId).to.be.not.null;
      expect(incrementCommand2['@type']).to.equal('inc');
      expect(incrementCommand2.delta).to.equal(-10);
      expect(incrementCommand2.targetNodeId).to.equal('target2');
      expect(isIncrementCommand(incrementCommand2)).to.be.true;
    });

    it('should create correct InsertCommand', () => {
      const insertCommand1 = createInsertCommand('list1', 'foo', ListPosition.last());
      expect(insertCommand1.commandId).to.be.not.null;
      expect(insertCommand1['@type']).to.equal('insert');
      expect(insertCommand1.value).to.equal('foo');
      expect(insertCommand1.targetNodeId).to.equal('list1');
      expect(insertCommand1.position).to.deep.equal({ after: null, before: EDGE });
      expect(isInsertCommand(insertCommand1)).to.be.true;

      const insertCommand2 = createInsertCommand('list2', 'bar', ListPosition.first());
      expect(insertCommand2.commandId).to.be.not.null;
      expect(insertCommand2['@type']).to.equal('insert');
      expect(insertCommand2.value).to.equal('bar');
      expect(insertCommand2.targetNodeId).to.equal('list2');
      expect(insertCommand2.position).to.deep.equal({ after: EDGE, before: null });
      expect(isInsertCommand(insertCommand2)).to.be.true;

      const insertCommand3 = createInsertCommand('list3', 'baz', ListPosition.after({ id: 'element1' }));
      expect(insertCommand3.commandId).to.be.not.null;
      expect(insertCommand3['@type']).to.equal('insert');
      expect(insertCommand3.value).to.equal('baz');
      expect(insertCommand3.targetNodeId).to.equal('list3');
      expect(insertCommand3.position).to.deep.equal({ after: 'element1', before: null });
      expect(isInsertCommand(insertCommand3)).to.be.true;
    });

    it('should create correct RemoveCommand', () => {
      const removeCommand1 = createRemoveCommand('element1', 'parent1');
      expect(removeCommand1.commandId).to.be.not.null;
      expect(removeCommand1['@type']).to.equal('remove');
      expect(removeCommand1.targetNodeId).to.equal('element1');
      expect(removeCommand1.expectedParentId).to.equal('parent1');
      expect(isRemoveCommand(removeCommand1)).to.be.true;

      const removeCommand2 = createRemoveCommand('element2', ZERO);
      expect(removeCommand2.commandId).to.be.not.null;
      expect(removeCommand2['@type']).to.equal('remove');
      expect(removeCommand2.targetNodeId).to.equal('element2');
      expect(removeCommand2.expectedParentId).to.equal(ZERO);
      expect(isRemoveCommand(removeCommand2)).to.be.true;
    });

    it('should create correct AdoptAtCommand', () => {
      const adoptAtCommand1 = createAdoptAtCommand('parent1', 'child1', ListPosition.last());
      expect(adoptAtCommand1.commandId).to.be.not.null;
      expect(adoptAtCommand1['@type']).to.equal('at');
      expect(adoptAtCommand1.targetNodeId).to.equal('parent1');
      expect(adoptAtCommand1.childId).to.equal('child1');
      expect(adoptAtCommand1.position).to.deep.equal({ after: null, before: EDGE });
      expect(isAdoptAtCommand(adoptAtCommand1)).to.be.true;

      const adoptAtCommand2 = createAdoptAtCommand('parent2', 'child2', ListPosition.before({ id: 'sibling1' }));
      expect(adoptAtCommand2.commandId).to.be.not.null;
      expect(adoptAtCommand2['@type']).to.equal('at');
      expect(adoptAtCommand2.targetNodeId).to.equal('parent2');
      expect(adoptAtCommand2.childId).to.equal('child2');
      expect(adoptAtCommand2.position).to.deep.equal({ after: null, before: 'sibling1' });
      expect(isAdoptAtCommand(adoptAtCommand2)).to.be.true;
    });

    it('should create correct PositionCondition', () => {
      const positionCondition1 = createPositionCondition('parent1', 'child1', ListPosition.first());
      expect(positionCondition1.commandId).to.be.not.null;
      expect(positionCondition1['@type']).to.equal('pos');
      expect(positionCondition1.targetNodeId).to.equal('parent1');
      expect(positionCondition1.childId).to.equal('child1');
      expect(positionCondition1.expectedPosition).to.deep.equal({ after: EDGE, before: null });
      expect(isPositionCondition(positionCondition1)).to.be.true;
    });

    it('should create correct TransactionCommand', () => {
      const setCommand = createSetCommand('target1', 'value1');
      const incrementCommand = createIncrementCommand('target2', 5);
      const commands = [setCommand, incrementCommand];

      const transactionCommand = createTransactionCommand(commands);
      expect(transactionCommand.commandId).to.be.not.null;
      expect(transactionCommand['@type']).to.equal('tx');
      expect(transactionCommand.targetNodeId).to.equal('');
      expect(transactionCommand.commands).to.have.length(2);
      expect(transactionCommand.commands[0]).to.equal(setCommand);
      expect(transactionCommand.commands[1]).to.equal(incrementCommand);
      expect(isTransactionCommand(transactionCommand)).to.be.true;
    });

    it('should create correct SnapshotCommand', () => {
      const nodes: Record<string, Node> = {
        '': {
          '@type': 'ListSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: ['node1', 'node2'],
          mapChildren: {},
        },
        node1: {
          '@type': 'ValueSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          value: 'value1',
          listChildren: [],
          mapChildren: {},
        },
        node2: {
          '@type': 'ValueSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          value: 'value2',
          listChildren: [],
          mapChildren: {},
        },
      };

      const snapshotCommand = createSnapshotCommand(nodes);
      expect(snapshotCommand.commandId).to.be.not.null;
      expect(snapshotCommand['@type']).to.equal('snapshot');
      expect(snapshotCommand.targetNodeId).to.equal('');
      expect(snapshotCommand.nodes).to.deep.equal(nodes);
      expect(isSnapshotCommand(snapshotCommand)).to.be.true;
    });

    it('should return true only when isSetCommand is called on an instance of SetCommand', () => {
      const setCommand = createSetCommand('target1', 'value');
      expect(isSetCommand(setCommand)).to.be.true;

      const incrementCommand = createIncrementCommand('target1', 5);
      expect(isSetCommand(incrementCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'invalid',
      };
      expect(isSetCommand(invalidCommand)).to.be.false;

      expect(isSetCommand(null)).to.be.false;
      expect(isSetCommand(undefined)).to.be.false;
      expect(isSetCommand('not-a-command')).to.be.false;
      expect(isSetCommand(42)).to.be.false;
    });

    it('should return true only when isIncrementCommand is called on an instance of IncrementCommand', () => {
      const incrementCommand = createIncrementCommand('target1', 10);
      expect(isIncrementCommand(incrementCommand)).to.be.true;

      const setCommand = createSetCommand('target1', 'value');
      expect(isIncrementCommand(setCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'set',
      };
      expect(isIncrementCommand(invalidCommand)).to.be.false;

      expect(isIncrementCommand(null)).to.be.false;
      expect(isIncrementCommand(undefined)).to.be.false;
      expect(isIncrementCommand({})).to.be.false;
    });

    it('should return true only when isInsertCommand is called on an instance of InsertCommand', () => {
      const insertCommand = createInsertCommand('list1', 'value', ListPosition.last());
      expect(isInsertCommand(insertCommand)).to.be.true;

      const setCommand = createSetCommand('target1', 'value');
      expect(isInsertCommand(setCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'remove',
      };
      expect(isInsertCommand(invalidCommand)).to.be.false;

      expect(isInsertCommand(null)).to.be.false;
      expect(isInsertCommand([])).to.be.false;
    });

    it('should return true only when isRemoveCommand is called on an instance of RemoveCommand', () => {
      const removeCommand = createRemoveCommand('element1', 'parent1');
      expect(isRemoveCommand(removeCommand)).to.be.true;

      const insertCommand = createInsertCommand('list1', 'value', ListPosition.last());
      expect(isRemoveCommand(insertCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'insert',
      };
      expect(isRemoveCommand(invalidCommand)).to.be.false;

      expect(isRemoveCommand(null)).to.be.false;
      expect(isRemoveCommand('not-a-command')).to.be.false;
    });

    it('should return true only when isSnapshotCommand is called on an instance of SnapshotCommand', () => {
      const nodes: Record<string, Node> = {
        '': {
          '@type': 'ValueSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          value: 42,
          listChildren: [],
          mapChildren: {},
        },
      };
      const snapshotCommand = createSnapshotCommand(nodes);
      expect(isSnapshotCommand(snapshotCommand)).to.be.true;

      const setCommand = createSetCommand('target1', 'value');
      expect(isSnapshotCommand(setCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'set',
      };
      expect(isSnapshotCommand(invalidCommand)).to.be.false;

      expect(isSnapshotCommand(null)).to.be.false;
      expect(isSnapshotCommand(undefined)).to.be.false;
      expect(isSnapshotCommand('not-a-command')).to.be.false;
      expect(isSnapshotCommand(true)).to.be.false;
    });

    it('should return true only when isTransactionCommand is called on an instance of TransactionCommand', () => {
      const commands = [createSetCommand('target1', 'value1'), createIncrementCommand('target2', 5)];
      const transactionCommand = createTransactionCommand(commands);
      expect(isTransactionCommand(transactionCommand)).to.be.true;

      const setCommand = createSetCommand('target1', 'value');
      expect(isTransactionCommand(setCommand)).to.be.false;

      // Missing commands array
      const invalidCommand1 = {
        commandId: 'id',
        targetNodeId: '',
        '@type': 'tx',
      };
      expect(isTransactionCommand(invalidCommand1)).to.be.false;

      // Wrong targetNodeId
      const invalidCommand2 = {
        commandId: 'id',
        targetNodeId: 'notEmpty',
        '@type': 'tx',
        commands: [],
      };
      expect(isTransactionCommand(invalidCommand2)).to.be.false;

      expect(isTransactionCommand(null)).to.be.false;
      expect(isTransactionCommand(undefined)).to.be.false;
    });

    it('should return true only when isValueCondition is called on an instance of ValueCondition', () => {
      const valueCondition = createValueCondition('target1', 'expectedValue');
      expect(isValueCondition(valueCondition)).to.be.true;

      const setCommand = createSetCommand('target1', 'value');
      expect(isValueCondition(setCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'set',
      };
      expect(isValueCondition(invalidCommand)).to.be.false;

      expect(isValueCondition(null)).to.be.false;
      expect(isValueCondition({})).to.be.false;
    });

    it('should return true only when isAdoptAtCommand is called on an instance of AdoptAtCommand', () => {
      const adoptAtCommand = createAdoptAtCommand('parent1', 'child1', ListPosition.last());
      expect(isAdoptAtCommand(adoptAtCommand)).to.be.true;

      const insertCommand = createInsertCommand('list1', 'value', ListPosition.last());
      expect(isAdoptAtCommand(insertCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'insert',
      };
      expect(isAdoptAtCommand(invalidCommand)).to.be.false;

      expect(isAdoptAtCommand(null)).to.be.false;
      expect(isAdoptAtCommand('not-a-command')).to.be.false;
    });

    it('should return true only when isPositionCondition is called on an instance of PositionCondition', () => {
      const positionCondition = createPositionCondition('parent1', 'child1', ListPosition.first());
      expect(isPositionCondition(positionCondition)).to.be.true;

      const adoptAtCommand = createAdoptAtCommand('parent1', 'child1', ListPosition.last());
      expect(isPositionCondition(adoptAtCommand)).to.be.false;

      const invalidCommand = {
        commandId: 'id',
        targetNodeId: 'target',
        '@type': 'at',
      };
      expect(isPositionCondition(invalidCommand)).to.be.false;

      expect(isPositionCondition(null)).to.be.false;
      expect(isPositionCondition({})).to.be.false;
    });
  });

  describe('ListPosition', () => {
    it('should create correct first position', () => {
      const position = ListPosition.first();
      expect(position).to.deep.equal({ after: EDGE, before: null });
    });

    it('should create correct last position', () => {
      const position = ListPosition.last();
      expect(position).to.deep.equal({ after: null, before: EDGE });
    });

    it('should create correct after position', () => {
      const position1 = ListPosition.after({ id: 'element1' });
      expect(position1).to.deep.equal({ after: 'element1', before: null });

      const position2 = ListPosition.after(null);
      expect(position2).to.deep.equal({ after: EDGE, before: null });
    });

    it('should create correct before position', () => {
      const position1 = ListPosition.before({ id: 'element1' });
      expect(position1).to.deep.equal({ after: null, before: 'element1' });

      const position2 = ListPosition.before(null);
      expect(position2).to.deep.equal({ after: null, before: EDGE });
    });

    it('should create correct between position', () => {
      const position1 = ListPosition.between({ id: 'element1' }, { id: 'element2' });
      expect(position1).to.deep.equal({ after: 'element1', before: 'element2' });

      const position2 = ListPosition.between(null, { id: 'element1' });
      expect(position2).to.deep.equal({ after: EDGE, before: 'element1' });

      const position3 = ListPosition.between({ id: 'element1' }, null);
      expect(position3).to.deep.equal({ after: 'element1', before: EDGE });

      const position4 = ListPosition.between(null, null);
      expect(position4).to.deep.equal({ after: EDGE, before: EDGE });
    });
  });

  describe('Constants', () => {
    it('should have correct ZERO constant', () => {
      expect(ZERO).to.equal('');
    });

    it('should have correct EDGE constant', () => {
      expect(EDGE).to.equal('');
    });
  });
});
