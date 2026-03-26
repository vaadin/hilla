import { describe, expect, it } from 'vitest';
import {
  createAdoptAtCommand,
  createClearCommand,
  createIncrementCommand,
  createInsertCommand,
  createPutCommand,
  createRemoveByKeyCommand,
  createRemoveCommand,
  createSetCommand,
  createSnapshotCommand,
  createTransactionCommand,
  createValueCondition,
  ListPosition,
  type Node,
} from '../src/commands.js';
import { applyCommand, emptyTree, getListChildren, getMapChildren, getNodeValue } from '../src/NodeTree.js';

describe('NodeTree', () => {
  describe('emptyTree', () => {
    it('should create a tree with a root node', () => {
      const tree = emptyTree();
      expect(tree.size).to.equal(1);
      expect(tree.has('')).to.be.true;
    });

    it('should create a tree with a root value', () => {
      const tree = emptyTree(42);
      expect(getNodeValue(tree, '')).to.equal(42);
    });
  });

  describe('applyCommand - set', () => {
    it('should set the root value', () => {
      const tree = emptyTree(0);
      const result = applyCommand(tree, createSetCommand('', 42));
      expect(result).not.to.be.null;
      expect(getNodeValue(result!, '')).to.equal(42);
    });

    it('should return null for non-existent node', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createSetCommand('nonexistent', 42));
      expect(result).to.be.null;
    });
  });

  describe('applyCommand - inc', () => {
    it('should increment a numeric value', () => {
      const tree = emptyTree(10);
      const result = applyCommand(tree, createIncrementCommand('', 5));
      expect(result).not.to.be.null;
      expect(getNodeValue(result!, '')).to.equal(15);
    });

    it('should decrement with negative delta', () => {
      const tree = emptyTree(10);
      const result = applyCommand(tree, createIncrementCommand('', -3));
      expect(result).not.to.be.null;
      expect(getNodeValue(result!, '')).to.equal(7);
    });

    it('should return null for non-numeric value', () => {
      const tree = emptyTree('not a number');
      const result = applyCommand(tree, createIncrementCommand('', 5));
      expect(result).to.be.null;
    });

    it('should treat undefined value as 0 for increment', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createIncrementCommand('', 5));
      expect(result).not.to.be.null;
      expect(getNodeValue(result!, '')).to.equal(5);
    });
  });

  describe('applyCommand - insert', () => {
    it('should insert at last position', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.last()));
      expect(result).not.to.be.null;
      const children = getListChildren(result!, '');
      expect(children).to.have.length(1);
      const childValue = getNodeValue(result!, children[0]);
      expect(childValue).to.equal('Alice');
    });

    it('should insert at first position', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createInsertCommand('', 'Bob', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.first()))!;
      const children = getListChildren(tree, '');
      expect(children).to.have.length(2);
      expect(getNodeValue(tree, children[0])).to.equal('Alice');
      expect(getNodeValue(tree, children[1])).to.equal('Bob');
    });

    it('should insert multiple items', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Bob', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Charlie', ListPosition.last()))!;
      const children = getListChildren(tree, '');
      expect(children).to.have.length(3);
      expect(getNodeValue(tree, children[0])).to.equal('Alice');
      expect(getNodeValue(tree, children[1])).to.equal('Bob');
      expect(getNodeValue(tree, children[2])).to.equal('Charlie');
    });
  });

  describe('applyCommand - remove', () => {
    it('should remove a list child', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.last()))!;
      const [childId] = getListChildren(tree, '');
      tree = applyCommand(tree, createRemoveCommand(childId, ''))!;
      expect(getListChildren(tree, '')).to.have.length(0);
    });

    it('should return null when removing root node (no parent)', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createRemoveCommand('', ''));
      expect(result).to.be.null;
    });
  });

  describe('applyCommand - clear', () => {
    it('should clear all children', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Bob', ListPosition.last()))!;
      tree = applyCommand(tree, createClearCommand(''))!;
      expect(getListChildren(tree, '')).to.have.length(0);
    });
  });

  describe('applyCommand - put', () => {
    it('should put a key-value pair', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createPutCommand('', 'name', 'Alice'))!;
      const mapChildren = getMapChildren(result, '');
      expect(mapChildren.size).to.equal(1);
      expect(mapChildren.has('name')).to.be.true;
      const childId = mapChildren.get('name')!;
      expect(getNodeValue(result, childId)).to.equal('Alice');
    });

    it('should replace existing key', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createPutCommand('', 'name', 'Alice'))!;
      tree = applyCommand(tree, createPutCommand('', 'name', 'Bob'))!;
      const mapChildren = getMapChildren(tree, '');
      expect(mapChildren.size).to.equal(1);
      const childId = mapChildren.get('name')!;
      expect(getNodeValue(tree, childId)).to.equal('Bob');
    });
  });

  describe('applyCommand - removeKey', () => {
    it('should remove a key from map children', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createPutCommand('', 'name', 'Alice'))!;
      tree = applyCommand(tree, createRemoveByKeyCommand('', 'name'))!;
      expect(getMapChildren(tree, '').size).to.equal(0);
    });

    it('should return null when removing non-existent key', () => {
      const tree = emptyTree();
      const result = applyCommand(tree, createRemoveByKeyCommand('', 'nonexistent'));
      expect(result).to.be.null;
    });
  });

  describe('applyCommand - snapshot', () => {
    it('should replace entire tree', () => {
      const tree = emptyTree(42);
      const nodes: Record<string, Node> = {
        '': {
          '@type': 'ListSignal',
          parent: null,
          lastUpdate: null,
          scopeOwner: null,
          listChildren: ['child1'],
          mapChildren: {},
        },
        child1: {
          '@type': 'ValueSignal',
          parent: '',
          lastUpdate: null,
          scopeOwner: null,
          value: 'Alice',
          listChildren: [],
          mapChildren: {},
        },
      };
      const result = applyCommand(tree, createSnapshotCommand(nodes))!;
      expect(result.size).to.equal(2);
      expect(getListChildren(result, '')).to.deep.equal(['child1']);
      expect(getNodeValue(result, 'child1')).to.equal('Alice');
    });
  });

  describe('applyCommand - transaction', () => {
    it('should apply all commands atomically', () => {
      const tree = emptyTree(0);
      const commands = [createSetCommand('', 10), createIncrementCommand('', 5)];
      const result = applyCommand(tree, createTransactionCommand(commands))!;
      expect(getNodeValue(result, '')).to.equal(15);
    });

    it('should return null if any command fails', () => {
      const tree = emptyTree(0);
      const commands = [createSetCommand('', 10), createSetCommand('nonexistent', 20)];
      const result = applyCommand(tree, createTransactionCommand(commands));
      expect(result).to.be.null;
    });
  });

  describe('applyCommand - value condition', () => {
    it('should pass when value matches', () => {
      const tree = emptyTree(42);
      const result = applyCommand(tree, createValueCondition('', 42));
      expect(result).to.equal(tree);
    });

    it('should fail when value does not match', () => {
      const tree = emptyTree(42);
      const result = applyCommand(tree, createValueCondition('', 99));
      expect(result).to.be.null;
    });
  });

  describe('applyCommand - adoptAt', () => {
    it('should move a child to a new position', () => {
      let tree = emptyTree();
      tree = applyCommand(tree, createInsertCommand('', 'Alice', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Bob', ListPosition.last()))!;
      tree = applyCommand(tree, createInsertCommand('', 'Charlie', ListPosition.last()))!;
      const children = getListChildren(tree, '');
      expect(children).to.have.length(3);

      // Move the last child to first position
      const [, , lastChildId] = children;
      tree = applyCommand(tree, createAdoptAtCommand('', lastChildId, ListPosition.first()))!;
      const newChildren = getListChildren(tree, '');
      expect(newChildren).to.have.length(3);
      expect(getNodeValue(tree, newChildren[0])).to.equal('Charlie');
      expect(getNodeValue(tree, newChildren[1])).to.equal('Alice');
      expect(getNodeValue(tree, newChildren[2])).to.equal('Bob');
    });
  });

  describe('helper functions', () => {
    it('getNodeValue should return undefined for non-existent node', () => {
      const tree = emptyTree();
      expect(getNodeValue(tree, 'nonexistent')).to.be.undefined;
    });

    it('getListChildren should return empty array for non-existent node', () => {
      const tree = emptyTree();
      expect(getListChildren(tree, 'nonexistent')).to.deep.equal([]);
    });

    it('getMapChildren should return empty map for non-existent node', () => {
      const tree = emptyTree();
      expect(getMapChildren(tree, 'nonexistent').size).to.equal(0);
    });
  });
});
