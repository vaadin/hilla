import {
  _fromString,
  type AbstractFieldStrategy,
  type AbstractModel,
  Binder,
  type BinderConfiguration,
  getBinderNode,
  getDefaultFieldStrategy,
  hasFromString,
  type ModelConstructor,
} from '@hilla/form';
import { useEffect, useRef, useState } from 'react';

const dummyElement = document.createElement('a');

const strategyRegistry = new WeakMap<HTMLElement, AbstractFieldStrategy>();

function getStrategy<T, M extends AbstractModel<T>>(field: HTMLElement, model: M): AbstractFieldStrategy<T> {
  let strategy = strategyRegistry.get(field);

  if (!strategy || strategy.model !== model) {
    strategy = getDefaultFieldStrategy(field, model);
    strategyRegistry.set(field, strategy);
  }

  return strategy;
}

export type FieldDirectiveResult = Readonly<{
  name: string;
  onBlur(): void;
  onChange(): void;
  onInput(): void;
  ref(element: HTMLElement | null): void;
}>;

export type FieldDirective = <M extends AbstractModel<unknown>>(model: M) => FieldDirectiveResult;

export type UseBinderResult<T, M extends AbstractModel<T>> = Readonly<{
  binder: Binder<T, M>;
  field: FieldDirective;
}>;

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): UseBinderResult<T, M> {
  const [binder, setBinder] = useState(() => new Binder(dummyElement, Model, config));
  const ref = useRef<HTMLElement | null>();

  useEffect(() => {
    setBinder(binder);
  }, [
    binder.value,
    binder.defaultValue,
    binder.submitting,
    binder.validating,
    binder.visited,
    binder.ownErrors,
    binder.validators,
  ]);

  useEffect(() => {
    if (ref.current) {
      binder.context = ref.current;
    }
  }, [ref.current, binder]);

  return {
    binder,
    field(model: AbstractModel<unknown>) {
      const node = getBinderNode(model);

      let field: HTMLElement | null;

      const updateValueEvent = () => {
        if (field) {
          const elementValue = getStrategy(field, model).value;
          node.value =
            typeof elementValue === 'string' && hasFromString(model) ? model[_fromString](elementValue) : elementValue;
        }
      };

      return {
        name: node.name,
        onBlur() {
          updateValueEvent();
          node.visited = true;
        },
        onChange: updateValueEvent,
        onInput: updateValueEvent,
        ref(element: HTMLElement | null) {
          field = element;
        },
      };
    },
  };
}
