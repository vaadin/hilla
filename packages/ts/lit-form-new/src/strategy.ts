import { computed, effect, type ReadonlySignal, signal } from '@preact/signals-core';
import m, { BooleanModel, type Model, type PrimitiveModel } from '@vaadin/hilla-models/models.js';
import type { ValidatableHTMLElement, ValidationError } from '@vaadin/hilla-models/validators.js';

export type FieldElement<T = unknown> = ValidatableHTMLElement & {
  value: T;
};

export interface FieldStrategy<T = unknown, V = unknown> {
  eventName: string;
  getValue(element: FieldElement<T>, model: Model<V>, event: Event): V;
  setValue(element: FieldElement<T>, value: V): void;
}

export type BoundFieldStrategy<V> = Readonly<{
  value: V;
  errors: readonly ValidationError[];
}>;

function validate(element: FieldElement, model: Model, value: unknown): readonly ValidationError[] {
  return m
    .validators(model)
    .map((validator) => (!validator.validate(value, element.validity) ? validator.error(value) : null))
    .filter((error) => error !== null);
}

export function bindStrategy<T, V>(
  element: FieldElement<T>,
  model: Model<V>,
  { eventName, getValue, setValue }: FieldStrategy<T, V>,
): ReadonlySignal<BoundFieldStrategy<V>> {
  const value = signal(m.value(model));
  const errors = computed(() => validate(element, model, value.value));

  const validators = m.validators(model);

  for (const validator of validators) {
    validator.bind(element);
  }

  let paused = false;

  effect(() => {
    if (!paused) {
      m.value(model, value.value);
    }
  });

  effect(() => {
    paused = true;
    value.value = m.value(model);
    setValue(element, value.value);
    paused = false;
  });

  element.addEventListener(eventName, (event) => {
    event.preventDefault();
    event.stopPropagation();
    value.value = getValue(element, model, event);
  });

  return computed(() => ({
    value: value.value,
    errors: errors.value,
  }));
}

export const GenericFieldStrategy: FieldStrategy = {
  eventName: 'input',
  getValue<T extends boolean | number | string>(element: FieldElement<string>, model: PrimitiveModel<T>): T {
    return m.parse(model, element.value);
  },
  setValue<T extends boolean | number | string>(element: FieldElement<string>, value: T): void {
    element.value = String(value);
  },
};

export type CheckedFieldElement<T> = FieldElement<T> & Pick<HTMLInputElement, 'checked'>;

export const CheckedFieldStrategy: FieldStrategy = {
  eventName: 'change',
  getValue<T>(element: CheckedFieldElement<T>, model: Model<T>): T | undefined {
    if (model instanceof BooleanModel) {
      return element.checked as T;
    }

    if (!element.checked) {
      return m.isOptional(model) ? undefined : m.defaultValue(model);
    }

    return element.value;
  },
  setValue<T>(element: CheckedFieldElement<string>, value: T | undefined) {
    element.checked = /^(true|on)$/iu.test(String(value));
  },
};

export type SelectFieldElement<T> = FieldElement<T> & Pick<HTMLSelectElement, 'selectedOptions' | 'value'>;

export const SelectFieldStrategy: FieldStrategy = {
  eventName: 'change',
  getValue<T>(element: SelectFieldElement<T>, model: Model<T>): T | undefined {
    if (element.selectedOptions.length === 0) {
      return m.isOptional(model) ? undefined : m.defaultValue(model);
    }

    return element.selectedOptions[0].value as T;
  },
  setValue<T>(element: SelectFieldElement<T>, value: T | undefined) {
    // element.value = String(value);
  },
};
