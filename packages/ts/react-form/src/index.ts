import {
  _fromString,
  _validity,
  AbstractModel,
  type BinderConfiguration,
  BinderRoot,
  CHANGED,
  defaultValidity,
  getBinderNode,
  getDefaultFieldStrategy,
  hasFromString,
  isFieldElement,
  type ModelConstructor,
  type Validator,
  type ValueError,
  type ModelValue,
  type FieldStrategy,
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
    value: T;
    setDefaultValue(value: T): void;
    setValue(value: T): void;
    submit(): Promise<T | undefined>;
    reset(): void;
    clear(): void;
    read(value: T): void;
  }>;

type FieldState<T> = {
  value?: T;
  required: boolean;
  invalid: boolean;
  errorMessage: string;
  strategy?: FieldStrategy<T>;
};

function convertFieldValue<T extends AbstractModel<unknown>>(model: T, fieldValue: unknown) {
  return typeof fieldValue === 'string' && hasFromString(model) ? model[_fromString](fieldValue) : fieldValue;
}

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

function useFields<T, M extends AbstractModel<T>>(node: BinderNode<T, M>): FieldDirective {
  return useMemo(() => {
    const registry = new WeakMap<AbstractModel<any>, FieldState<any>>();

    return ((model: AbstractModel<any>) => {
      const n = getBinderNode(model);

      const fieldState: FieldState<unknown> = registry.get(model) ?? {
        value: undefined,
        required: false,
        invalid: false,
        errorMessage: '',
        strategy: undefined,
      };

      if (!registry.has(model)) {
        registry.set(model, fieldState);
      }

      if (fieldState.strategy) {
        const valueFromField = convertFieldValue(model, fieldState.value);
        if (valueFromField !== n.value && !(Number.isNaN(n.value) && Number.isNaN(valueFromField))) {
          fieldState.value = n.value;
          fieldState.strategy.value = n.value;
        }

        if (fieldState.required !== n.required) {
          fieldState.required = n.required;
          fieldState.strategy.required = n.required;
        }

        const firstError = n.ownErrors.at(0);
        // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
        const errorMessage = firstError?.message ?? '';
        if (fieldState.errorMessage !== errorMessage) {
          fieldState.errorMessage = errorMessage;
          fieldState.strategy.errorMessage = errorMessage;
        }

        if (fieldState.invalid !== n.invalid) {
          fieldState.invalid = n.invalid;
          fieldState.strategy.invalid = n.invalid;
        }
      }

      const updateValueEvent = () => {
        if (fieldState.strategy) {
          // When bad input is detected, skip reading new value in binder state
          fieldState.strategy.checkValidity();
          if (!fieldState.strategy.validity.badInput) {
            fieldState.value = fieldState.strategy.value;
          }
          n[_validity] = fieldState.strategy.validity;
          n.value = convertFieldValue(model, fieldState.value);
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
          if (!element) {
            return;
          }

          if (!isFieldElement(element)) {
            throw new TypeError(`Element '${element.localName}' is not a form element`);
          }

          fieldState.strategy = getDefaultFieldStrategy(element, model);
        },
      };
    }) as FieldDirective;
  }, [node]);
}

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): BinderControls<T, M> {
  const update = useUpdate();
  const binder = useMemo(() => new BinderRoot(Model, config), [Model]);
  const field = useFields(binder);

  useEffect(() => {
    binder.addEventListener(CHANGED.type, update);
  }, [binder]);

  return {
    ...getBinderNodeControls(binder),
    setDefaultValue: (defaultValue: T) => {
      binder.defaultValue = defaultValue;
    },
    value: binder.value,
    setValue: (value: T) => {
      binder.value = value;
    },
    clear: binder.clear.bind(binder),
    field,
    read: binder.read.bind(binder),
    reset: binder.reset.bind(binder),
    submit: binder.submit.bind(binder),
  };
}

export function useBinderNode<T, M extends AbstractModel<T>>(model: M): BinderNodeControls<T, M> {
  const binderNode = getBinderNode(model) as BinderNode<T, M>;
  const field = useFields(binderNode);
  return {
    ...getBinderNodeControls(binderNode),
    field,
  };
}
