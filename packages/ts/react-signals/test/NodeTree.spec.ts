import { describe, expect, it } from 'vitest';
import {
  createAdoptAtCommand,
  createClearCommand,
  createIncrementCommand,
  createInsertCommand,
  createPositionCondition,
  createPutCommand,
  createRemoveByKeyCommand,
  createRemoveCommand,
  createSetCommand,
  createSnapshotCommand,
  createTransactionCommand,
  createValueCondition,
  ListPosition,
  type Node,
  type SignalCommand,
} from '../src/commands.js';
import {
  applyCommand,
  emptyTree,
  findInsertIndex,
  getListChildren,
  getMapChildren,
  getNodeValue,
  ROOT,
  type NodeTree,
} from '../src/NodeTree.js';

function node(overrides: Partial<Node> = {}): Node {
  return {
    '@type': 'ValueSignal',
    parent: null,
    lastUpdate: null,
    scopeOwner: null,
    listChildren: [],
    mapChildren: {},
    ...overrides,
  };
}

function apply(tree: NodeTree, ...commands: readonly SignalCommand[]): NodeTree {
  let result = tree;

  for (const command of commands) {
    const next = applyCommand(result, command);
    expect(next, `command ${command['@type']} was expected to apply`).to.not.be.null;
    result = next!;
  }

  return result;
}

describe('@vaadin/hilla-react-signals', () => {
  describe('NodeTree', () => {
    it('should start with an empty root node', () => {
      const tree = emptyTree();

      expect(getNodeValue(tree, ROOT)).to.be.undefined;
      expect(getListChildren(tree, ROOT)).to.deep.equal([]);
      expect(getMapChildren(tree, ROOT)).to.deep.equal({});
    });

    it('should not mutate the tree it applies a command to', () => {
      const tree = emptyTree();
      const updated = apply(tree, createSetCommand(ROOT, 'value'));

      expect(getNodeValue(tree, ROOT)).to.be.undefined;
      expect(getNodeValue(updated, ROOT)).to.equal('value');
    });

    it('should reject a command that targets an unknown node', () => {
      expect(applyCommand(emptyTree(), createSetCommand('missing', 'value'))).to.be.null;
    });

    describe('value commands', () => {
      it('should set a value', () => {
        const tree = apply(emptyTree(), createSetCommand(ROOT, { name: 'Alice' }));
        expect(getNodeValue(tree, ROOT)).to.deep.equal({ name: 'Alice' });
      });

      it('should treat an absent value as zero when incrementing', () => {
        const tree = apply(emptyTree(), createIncrementCommand(ROOT, 5));
        expect(getNodeValue(tree, ROOT)).to.equal(5);
      });

      it('should add up increments', () => {
        const tree = apply(emptyTree(), createIncrementCommand(ROOT, 5), createIncrementCommand(ROOT, -2));
        expect(getNodeValue(tree, ROOT)).to.equal(3);
      });

      it('should reject incrementing a non-numeric value', () => {
        const tree = apply(emptyTree(), createSetCommand(ROOT, 'text'));
        expect(applyCommand(tree, createIncrementCommand(ROOT, 1))).to.be.null;
      });

      it('should compare condition values based on their content', () => {
        const tree = apply(emptyTree(), createSetCommand(ROOT, { name: 'Alice' }));

        expect(applyCommand(tree, createValueCondition(ROOT, { name: 'Alice' }))).to.equal(tree);
        expect(applyCommand(tree, createValueCondition(ROOT, { name: 'Bob' }))).to.be.null;
      });
    });

    describe('list commands', () => {
      it('should use the command id as the id of the inserted node', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const tree = apply(emptyTree(), insert);

        expect(getListChildren(tree, ROOT)).to.deep.equal([insert.commandId]);
        expect(getNodeValue(tree, insert.commandId)).to.equal('Alice');
        expect(tree.get(insert.commandId)?.parent).to.equal(ROOT);
      });

      it('should insert at the requested position', () => {
        const first = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const last = createInsertCommand(ROOT, 'Bob', ListPosition.last());
        const middle = createInsertCommand(ROOT, 'Charlie', ListPosition.after({ id: first.commandId }));
        const head = createInsertCommand(ROOT, 'Dave', ListPosition.first());

        const tree = apply(emptyTree(), first, last, middle, head);

        expect(getListChildren(tree, ROOT).map((id) => getNodeValue(tree, id))).to.deep.equal([
          'Dave',
          'Alice',
          'Charlie',
          'Bob',
        ]);
      });

      it('should reject an insert relative to an unknown sibling', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.after({ id: 'missing' }));
        expect(applyCommand(emptyTree(), insert)).to.be.null;
      });

      it('should remove a child and detach it from its parent', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        let tree = apply(emptyTree(), insert);

        tree = apply(tree, createRemoveCommand(insert.commandId, ROOT));

        expect(getListChildren(tree, ROOT)).to.deep.equal([]);
        expect(tree.has(insert.commandId)).to.be.false;
      });

      it('should reject removing a child of another parent', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const tree = apply(emptyTree(), insert);

        expect(applyCommand(tree, createRemoveCommand(insert.commandId, 'other'))).to.be.null;
      });

      it('should reject removing the root node', () => {
        expect(applyCommand(emptyTree(), createRemoveCommand(ROOT, ROOT))).to.be.null;
      });

      it('should move a child without losing its own children', () => {
        const first = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const second = createInsertCommand(ROOT, 'Bob', ListPosition.last());
        let tree = apply(emptyTree(), first, second);

        const grandChild = createInsertCommand(first.commandId, 'Grandchild', ListPosition.last());
        tree = apply(tree, grandChild);

        tree = apply(tree, createAdoptAtCommand(ROOT, first.commandId, ListPosition.last()));

        expect(getListChildren(tree, ROOT)).to.deep.equal([second.commandId, first.commandId]);
        expect(getListChildren(tree, first.commandId)).to.deep.equal([grandChild.commandId]);
        expect(getNodeValue(tree, grandChild.commandId)).to.equal('Grandchild');
      });

      it('should check the position of a child', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const tree = apply(emptyTree(), insert);

        expect(applyCommand(tree, createPositionCondition(ROOT, insert.commandId, ListPosition.first()))).to.equal(
          tree,
        );
        expect(applyCommand(tree, createPositionCondition(ROOT, 'missing', ListPosition.first()))).to.be.null;
      });
    });

    describe('map commands', () => {
      it('should use the command id as the id of a node added with a new key', () => {
        const put = createPutCommand(ROOT, 'name', 'Alice');
        const tree = apply(emptyTree(), put);

        expect(getMapChildren(tree, ROOT)).to.deep.equal({ name: put.commandId });
        expect(getNodeValue(tree, put.commandId)).to.equal('Alice');
      });

      it('should update the existing node when putting to an existing key', () => {
        const put = createPutCommand(ROOT, 'name', 'Alice');
        let tree = apply(emptyTree(), put);

        tree = apply(tree, createPutCommand(ROOT, 'name', 'Bob'));

        // The node of the entry is kept so that the child signal stays valid
        expect(getMapChildren(tree, ROOT)).to.deep.equal({ name: put.commandId });
        expect(getNodeValue(tree, put.commandId)).to.equal('Bob');
      });

      it('should remove an entry by key', () => {
        const put = createPutCommand(ROOT, 'name', 'Alice');
        let tree = apply(emptyTree(), put);

        tree = apply(tree, createRemoveByKeyCommand(ROOT, 'name'));

        expect(getMapChildren(tree, ROOT)).to.deep.equal({});
        expect(tree.has(put.commandId)).to.be.false;
      });

      it('should reject removing an unknown key', () => {
        expect(applyCommand(emptyTree(), createRemoveByKeyCommand(ROOT, 'missing'))).to.be.null;
      });
    });

    describe('clear', () => {
      it('should detach all children but keep the value of the node', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const put = createPutCommand(ROOT, 'name', 'Bob');
        let tree = apply(emptyTree(), createSetCommand(ROOT, 'kept'), insert, put);

        tree = apply(tree, createClearCommand(ROOT));

        expect(getListChildren(tree, ROOT)).to.deep.equal([]);
        expect(getMapChildren(tree, ROOT)).to.deep.equal({});
        expect(tree.has(insert.commandId)).to.be.false;
        expect(tree.has(put.commandId)).to.be.false;
        expect(getNodeValue(tree, ROOT)).to.equal('kept');
      });

      it('should accept clearing a node without children', () => {
        const tree = emptyTree();
        expect(applyCommand(tree, createClearCommand(ROOT))).to.equal(tree);
      });
    });

    describe('transaction', () => {
      it('should apply all commands of a transaction', () => {
        const insert = createInsertCommand(ROOT, 'Alice', ListPosition.last());
        const tree = apply(emptyTree(), createTransactionCommand([createSetCommand(ROOT, 'value'), insert]));

        expect(getNodeValue(tree, ROOT)).to.equal('value');
        expect(getListChildren(tree, ROOT)).to.deep.equal([insert.commandId]);
      });

      it('should apply none of the commands when one of them fails', () => {
        const transaction = createTransactionCommand([
          createSetCommand(ROOT, 'value'),
          createSetCommand('missing', 'value'),
        ]);

        expect(applyCommand(emptyTree(), transaction)).to.be.null;
      });
    });

    describe('snapshot', () => {
      it('should replace the whole tree', () => {
        const tree = apply(emptyTree(), createSetCommand(ROOT, 'replaced'));

        const snapshot = apply(
          tree,
          createSnapshotCommand({
            '': node({ listChildren: ['child'] }),
            child: node({ parent: '', value: 'Alice' }),
          }),
        );

        expect(getNodeValue(snapshot, ROOT)).to.be.undefined;
        expect(getListChildren(snapshot, ROOT)).to.deep.equal(['child']);
        expect(getNodeValue(snapshot, 'child')).to.equal('Alice');
      });

      it('should ignore alias nodes', () => {
        const snapshot = apply(
          emptyTree(),
          createSnapshotCommand({
            '': node({ listChildren: ['child'] }),
            child: node({ parent: '', value: 'Alice' }),
            alias: { '@type': 'Alias', target: 'child' } as unknown as Node,
          }),
        );

        expect(snapshot.has('alias')).to.be.false;
        expect(getNodeValue(snapshot, 'child')).to.equal('Alice');
      });
    });

    describe('findInsertIndex', () => {
      it('should resolve the edges of the list', () => {
        expect(findInsertIndex(['a', 'b'], ListPosition.first())).to.equal(0);
        expect(findInsertIndex(['a', 'b'], ListPosition.last())).to.equal(2);
      });

      it('should resolve a position relative to a child', () => {
        expect(findInsertIndex(['a', 'b'], ListPosition.after({ id: 'a' }))).to.equal(1);
        expect(findInsertIndex(['a', 'b'], ListPosition.before({ id: 'b' }))).to.equal(1);
        expect(findInsertIndex(['a', 'b'], ListPosition.between({ id: 'a' }, { id: 'b' }))).to.equal(1);
      });

      it('should not resolve a position that cannot be satisfied', () => {
        expect(findInsertIndex(['a', 'b'], ListPosition.after({ id: 'missing' }))).to.equal(-1);
        expect(findInsertIndex(['a', 'b'], ListPosition.between({ id: 'a' }, { id: 'missing' }))).to.equal(-1);
        expect(findInsertIndex(['a', 'b'], { after: null, before: null })).to.equal(-1);
      });
    });
  });
});
