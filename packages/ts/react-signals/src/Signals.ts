import type { Signal } from './core.js';
import { SharedSignal } from './ExtendedSignals';
import type { SetEvent, StateEvent } from './types';


export class ValueSignal<T> extends SharedSignal<T> {
  private readonly publish: (event: StateEvent) => Promise<boolean>;

  constructor(internalSignal: Signal, publish: (event: StateEvent) => Promise<boolean>) {
    super(() => internalSignal.value);

    this.publish = publish;
  }

  override get value() {
    return super.value;
  }

  override set value(value: T) {
    const id = crypto.randomUUID();
    const event: SetEvent = { id, set: 'id', value };
    this.publish(event).then(r => undefined );
  }

  compareAndSet(expectedValue: T, newValue: T): Promise<boolean> {
    const id = crypto.randomUUID();
    const event: SetEvent = {
      id,
      set: 'id',
      value: newValue,
      conditions: [{ id: 'id', value: expectedValue }],
    };

    return this.publish(event);
  }

  async update(updater: (value: T) => T): Promise<void> {
    while (!(await this.compareAndSet(this.value, updater(this.value)))) {}
  }
}

export class NumberSignal extends ValueSignal<number> {

  constructor(internalSignal: Signal, publish: (event: StateEvent) => Promise<boolean>) {
    super(internalSignal, publish);
  }

}
