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
  type Validator,
  type ValueError,
} from '@hilla/form';
import type { BinderNode } from '@hilla/form/BinderNode.js';
import { useMemo, useReducer } from 'react';

const strategyRegistry = new WeakMap<Element, AbstractFieldStrategy>();

function getStrategy<T, M extends AbstractModel<unknown>>(fld: HTMLElement, model: M): AbstractFieldStrategy<T> {
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

export type BinderNodeControls<T, M extends AbstractModel<T>> = Readonly<{
  defaultValue: T;
  dirty: boolean;
  errors: ReadonlyArray<ValueError<unknown>>;
  invalid: boolean;
  model: M;
  name: string;
  ownErrors: ReadonlyArray<ValueError<T>>;
  required: boolean;
  validators: ReadonlyArray<Validator<T>>;
  value?: T;
  visited: boolean;
  field(model: AbstractModel<unknown>): FieldDirectiveResult;
  setValidators(validators: ReadonlyArray<Validator<T>>): void;
  setValue(value: T | undefined): void;
  setVisited(visited: boolean): void;
  validate(): Promise<ReadonlyArray<ValueError<unknown>>>;
}>;

export type BinderControls<T, M extends AbstractModel<T>> = BinderNodeControls<T, M> &
  Readonly<{
    submit(): Promise<T | undefined>;
    reset(): void;
    clear(): void;
  }>;

function getBinderNodeControls<T, M extends AbstractModel<T>>(node: BinderNode<T, M>): BinderNodeControls<T, M> {
  const field = (model: AbstractModel<unknown>): FieldDirectiveResult => {
    const n = getBinderNode(model);

    let fld: HTMLElement | null;

    const updateValueEvent = () => {
      if (fld) {
        const elementValue = getStrategy(fld, model).value;
        n.value =
          typeof elementValue === 'string' && hasFromString(model) ? model[_fromString](elementValue) : elementValue;
      }
    };

    return {
      name: n.name,
      onBlur() {
        updateValueEvent();
        n.visited = true;
      },
      onChange: updateValueEvent,
      onInput: updateValueEvent,
      ref(element: HTMLElement | null) {
        fld = element;
      },
    };
  };

  return {
    defaultValue: node.defaultValue,
    dirty: node.dirty,
    errors: node.errors,
    field,
    invalid: node.invalid,
    model: node.model,
    name: node.name,
    ownErrors: node.ownErrors,
    required: node.required,
    setValidators(validators) {
      node.validators = validators;
    },
    setValue(value) {
      node.value = value;
    },
    setVisited(visited: boolean) {
      node.visited = visited;
    },
    validate: node.validate.bind(node),
    validators: node.validators,
    value: node.value,
    visited: node.visited,
  };
}

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): BinderControls<T, M> {
  const update = useUpdate();
  const binder = useMemo(() => {
    const b = new BinderRoot(Model, config);
    b.addEventListener(CHANGED.type, update);
    return b;
  }, [Model, config]);

  return {
    ...getBinderNodeControls(binder),
    clear: binder.clear.bind(binder),
    reset: binder.reset.bind(binder),
    submit: binder.submit.bind(binder),
  };
}

export function useBinderNode<T, M extends AbstractModel<T>>(model: M): BinderNodeControls<T, M> {
  return getBinderNodeControls(getBinderNode(model) as BinderNode<T, M>);
}
