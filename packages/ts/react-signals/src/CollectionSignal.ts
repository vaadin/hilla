import { FullStackSignal } from './FullStackSignal.js';

export abstract class CollectionSignal<T> extends FullStackSignal<T> {
  static {
    const { set, ...descriptor } = Reflect.getOwnPropertyDescriptor(this.prototype, 'value') ?? {};

    // eslint-disable-next-line accessor-pairs
    Reflect.defineProperty(this.prototype, 'value', {
      ...descriptor,
      set(v: any) {
        throw new Error('Value of the collection signals cannot be set.');
      },
    });
  }

  // @ts-expect-error: we removed the setter here
  declare readonly value: T;
}
