import { Signal, signal } from '@preact/signals-core';
import m, { $defaultValue, type $model, type Model, type Target, type Value } from '@vaadin/hilla-models';

export class Binder<M extends Model> extends Signal<Value<M>> {
  declare readonly [$model]: M;

  constructor(model: M) {
    super(model[$defaultValue] as Value<M>);
    m.attach(model, this as Target<Value<M>>);
  }

  get value(): Value<M> {
    return this[$model].value;
  }
}
