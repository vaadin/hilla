import { reduce } from 'itertools-ts';
import { beforeEach, describe, expect, it } from 'vitest';
import { signal, NodeSignal, value, object, effect } from '../src/index.js';

describe('NodeSignal', () => {
  it('creates a NodeSignal instance from regular type', () => {
    const sig = signal(1);
    expect(sig).to.be.instanceOf(NodeSignal);

    const events: number[] = [];

    effect(() => {
      events.push(value(sig));
    });

    value(sig, 2);
    value(sig, 30);
    value(sig, 10);

    expect(events).to.deep.equal([1, 2, 30, 10]);
  });

  describe('arrays', () => {
    let sig: NodeSignal<number[]>;

    beforeEach(() => {
      sig = signal([1, 2, 3]);
    });

    it('creates a NodeSignal instance from an array type', () => {
      expect(sig).to.be.instanceOf(NodeSignal);
      expect(value(sig)).to.be.deep.equal([1, 2, 3]);
      const firstElement = reduce.toFirst(sig.values());
      expect(firstElement).to.be.instanceOf(NodeSignal);
      expect(value(firstElement)).to.be.equal(1);
      value(firstElement, 10);
      expect(value(sig)).to.be.deep.equal([10, 2, 3]);
    });

    it('allows to add a new element to the array', () => {
      sig.push(4);
      expect(value(sig)).to.be.deep.equal([1, 2, 3, 4]);
      const lastElement = reduce.toLast(sig.values());
      expect(lastElement).to.be.instanceOf(NodeSignal);
      expect(value(lastElement)).to.be.equal(4);
      value(lastElement, 5);
      expect(value(sig)).to.be.deep.equal([1, 2, 3, 5]);
    });

    it('allows to remove the last element from the array', () => {
      const element = sig.pop();
      expect(element).to.be.instanceOf(NodeSignal);
      expect(value(element!)).to.be.equal(3);
      expect(value(sig)).to.be.deep.equal([1, 2]);
    });

    it('allows to remove the first element from the array', () => {
      const element = sig.shift();
      expect(element).to.be.instanceOf(NodeSignal);
      expect(value(element!)).to.be.equal(1);
      expect(value(sig)).to.be.deep.equal([2, 3]);
    });

    it('allows to add a new element to the beginning of the array', () => {
      sig.unshift(0);
      expect(value(sig)).to.be.deep.equal([0, 1, 2, 3]);
      const firstElement = reduce.toFirst(sig.values());
      expect(firstElement).to.be.instanceOf(NodeSignal);
      expect(value(firstElement)).to.be.equal(0);
      value(firstElement, 10);
      expect(value(sig)).to.be.deep.equal([10, 1, 2, 3]);
    });
  });

  describe('objects', () => {
    type TestObject = {
      foo: number;
      bar: string[];
      baz: {
        qux: number;
        quux: string;
      };
    };

    let sig: NodeSignal<TestObject>;

    beforeEach(() => {
      sig = signal({
        foo: 1,
        bar: ['a', 'b', 'c'],
        baz: {
          qux: 2,
          quux: 'hello',
        },
      });
    });

    it('creates a NodeSignal instance from an object type', () => {
      expect(sig).to.be.instanceOf(NodeSignal);
      expect(value(sig)).to.be.deep.equal({
        foo: 1,
        bar: ['a', 'b', 'c'],
        baz: {
          qux: 2,
          quux: 'hello',
        },
      });
      expect(value(sig.foo)).to.be.equal(1);
      expect(value(sig.bar)).to.be.deep.equal(['a', 'b', 'c']);
      expect(value(sig.baz)).to.be.deep.equal({
        qux: 2,
        quux: 'hello',
      });
      expect(value(sig.baz.quux)).to.be.equal('hello');
    });

    it('allows adding a new property to the object', () => {
      const newSignal = object.set(sig, 'newProp', 42);
      expect(value(newSignal.newProp)).to.be.equal(42);
      expect(value(newSignal)).to.be.deep.equal({
        foo: 1,
        bar: ['a', 'b', 'c'],
        baz: {
          qux: 2,
          quux: 'hello',
        },
        newProp: 42,
      });
    });
  });
});
