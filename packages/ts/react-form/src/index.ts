import {
  _fromString,
  type AbstractFieldStrategy,
  type AbstractModel,
  type BinderConfiguration,
  BinderRoot,
  CHANGED,
  getBinderNode,
  getDefaultFieldStrategy,
  hasFromString,
  isFieldElement,
  type ModelConstructor,
} from '@hilla/form';
import { useMemo, useReducer } from 'react';

const strategyRegistry = new WeakMap<Element, AbstractFieldStrategy>();

function getStrategy<T, M extends AbstractModel<T>>(fld: HTMLElement, model: M): AbstractFieldStrategy<T> {
  if (!isFieldElement<T>(fld)) {
    throw new TypeError(`Element '${fld.localName}' is not a form element`);
  }

  let strategy = strategyRegistry.get(fld);

  if (!strategy || strategy.model !== model) {
    strategy = getDefaultFieldStrategy(fld, model);
    strategyRegistry.set(fld, strategy);
  }

  return strategy;
}

function useUpdate() {
  const [_, update] = useReducer((x) => !x, true);
  return update;
}

export type FieldDirectiveResult = Readonly<{
  name: string;
  onBlur(): void;
  onChange(): void;
  onInput(): void;
  ref(element: HTMLElement | null): void;
}>;

export function field(model: AbstractModel<unknown>): FieldDirectiveResult {
  const node = getBinderNode(model);

  let fld: HTMLElement | null;

  const updateValueEvent = () => {
    if (fld) {
      const elementValue = getStrategy(fld, model).value;
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
      fld = element;
    },
  };
}

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): M {
  const update = useUpdate();
  const binder = useMemo(() => {
    const b = new BinderRoot(Model, config);
    b.addEventListener(CHANGED.type, update);
    return b;
  }, [Model, config]);

  return binder.model;
}
