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
  type FieldStrategy,
} from '@hilla/form';
import type { BinderNode } from '@hilla/form/BinderNode.js';
import { useEffect, useMemo, useReducer, useRef } from 'react';

function useUpdate() {
  const [_, update] = useReducer((x) => ++x, 0);
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
  addValidator(validator: Validator<T>): void;
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
  element?: HTMLElement;
  updateValue: () => void;
  markVisited: () => void;
  ref: (element: HTMLElement | null) => void;
};

function convertFieldValue<T extends AbstractModel<unknown>>(model: T, fieldValue: unknown) {
  return typeof fieldValue === 'string' && hasFromString(model) ? model[_fromString](fieldValue) : fieldValue;
}

function getBinderNodeControls<T, M extends AbstractModel<T>>(
  node: BinderNode<T, M>,
): Omit<BinderNodeControls<T, M>, 'field'> {
  return {
    addValidator: node.addValidator.bind(node),
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
        element: undefined,
        strategy: undefined,
        updateValue: () => {
          if (fieldState.strategy) {
            // Remove invalid flag, so that .checkValidity() in Vaadin Components
            // does not interfere with errors from Hilla.
            fieldState.strategy.invalid = false;
            // When bad input is detected, skip reading new value in binder state
            fieldState.strategy.checkValidity();
            if (!fieldState.strategy.validity.badInput) {
              fieldState.value = fieldState.strategy.value;
            }
            n[_validity] = fieldState.strategy.validity;
            n.value = convertFieldValue(model, fieldState.value);
          }
        },
        markVisited: () => {
          n.visited = true;
        },
        ref(element: HTMLElement | null) {
          if (!element) {
            fieldState.element?.removeEventListener('change', fieldState.updateValue);
            fieldState.element?.removeEventListener('input', fieldState.updateValue);
            fieldState.element?.removeEventListener('blur', fieldState.markVisited);
            fieldState.strategy?.removeEventListeners();
            fieldState.element = undefined;
            fieldState.strategy = undefined;
            return;
          }

          if (!isFieldElement(element)) {
            throw new TypeError(`Element '${element.localName}' is not a form element`);
          }

          if (fieldState.element !== element) {
            fieldState.element = element;
            fieldState.element.addEventListener('change', fieldState.updateValue);
            fieldState.element.addEventListener('input', fieldState.updateValue);
            fieldState.element.addEventListener('blur', fieldState.markVisited);
            fieldState.strategy = getDefaultFieldStrategy(element, model);
          }
        },
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

        // Make sure invalid state is always in sync
        fieldState.invalid = n.invalid;
        fieldState.strategy.invalid = n.invalid;
      }

      return {
        name: n.name,
        ref: fieldState.ref,
      };
    }) as FieldDirective;
  }, [node]);
}

type MutableBinderConfiguration<T> = {
  -readonly [K in keyof BinderConfiguration<T>]: BinderConfiguration<T>[K];
};

export function useBinder<T, M extends AbstractModel<T>>(
  Model: ModelConstructor<T, M>,
  config?: BinderConfiguration<T>,
): BinderControls<T, M> {
  const configRef = useRef<MutableBinderConfiguration<T>>({});
  configRef.current.onSubmit = config?.onSubmit;
  configRef.current.onChange = config?.onChange;
  const update = useUpdate();
  const binder = useMemo(() => new BinderRoot(Model, configRef.current), [Model]);
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

export function useBinderNode<M extends AbstractModel<any>>(model: M): BinderNodeControls<ReturnType<M['valueOf']>, M> {
  const binderNode = getBinderNode(model);
  const field = useFields(binderNode);
  return {
    ...getBinderNodeControls(binderNode),
    field,
  };
}
