import { FullStackSignal } from './FullStackSignal.js';

/**
 * A full-stack signal that holds an arbitrary value.
 */
export class ValueSignal<T> extends FullStackSignal<T> {
  set(value: T): void {
    this.value = value;
  }

  replace(expected: T, newValue: T): void {
    this.server.update({});
  }

  update(callback: (value: T) => T): void {
    throw new Error('Method not implemented.');
  }
}
