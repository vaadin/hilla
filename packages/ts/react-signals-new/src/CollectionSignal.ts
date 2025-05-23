import { FullStackSignal } from './FullStackSignal.js';

/**
 * A {@link FullStackSignal} that represents a collection of values.
 *
 * @typeParam T - The type of the values in the collection.
 */
export abstract class CollectionSignal<T> extends FullStackSignal<T> {
  override get value(): T {
    return super.value;
  }

  /**
   * @readonly
   */
  override set value(_: never) {
    throw new Error('Value of the collection signals cannot be set.');
  }
}
