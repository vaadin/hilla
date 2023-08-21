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
import { useEffect, useMemo, useReducer, useRef } from 'react';

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

export type FieldDirective = (model: AbstractModel<any>) => FieldDirectiveResult;

export type BinderNodeControls<T, M extends AbstractModel<T>> = Readonly<{
  defaultValue: T;
  dirty: boolean;
  errors: ReadonlyArray<ValueError<unknown>>;
  invalid: boolean;
  model: M;
  name: string;
  field: FieldDirective;
  ownErrors: ReadonlyArray<ValueError<T>>;
  required: boolean;
  validators: ReadonlyArray<Validator<T>>;
  value?: T;
  visited: boolean;
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

function getBinderNodeControls<T, M extends AbstractModel<T>>(
  node: BinderNode<T, M>,
): Omit<BinderNodeControls<T, M>, 'field'> {
  return {
    defaultValue: node.defaultValue,
    dirty: node.dirty,
    errors: node.errors,
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

function useFields(): FieldDirective {
  const registry = useRef(new WeakMap<HTMLElement, AbstractFieldStrategy>());

  return (model) => {
    const n = getBinderNode(model);

    let fld: HTMLElement | null;

    const updateValueEvent = () => {
      if (fld) {
        if (!isFieldElement(fld)) {
          throw new TypeError(`Element '${fld.localName}' is not a form element`);
        }

        let strategy = registry.current.get(fld);

        if (!strategy || strategy.model !== model) {
          strategy = getDefaultFieldStrategy(fld, model);
          registry.current.set(fld, strategy);
        }

        const elementValue = strategy.value;

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
}

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): BinderControls<T, M> {
  const update = useUpdate();
  const field = useFields();
  const binder = useMemo(() => new BinderRoot(Model, config), [Model, config]);

  useEffect(() => {
    binder.addEventListener(CHANGED.type, update);
  }, [binder]);

  return {
    ...getBinderNodeControls(binder),
    clear: binder.clear.bind(binder),
    field,
    reset: binder.reset.bind(binder),
    submit: binder.submit.bind(binder),
  };
}

export function useBinderNode<T, M extends AbstractModel<T>>(model: M): BinderNodeControls<T, M> {
  const field = useFields();
  return {
    ...getBinderNodeControls(getBinderNode(model) as BinderNode<T, M>),
    field,
  };
}
