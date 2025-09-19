/* eslint-disable accessor-pairs,no-void,sort-keys */
import { ArrayModel, BooleanModel, type Model, NumberModel, ObjectModel, StringModel } from '@vaadin/hilla-models';
import { type ElementPart, noChange, nothing, type PropertyPart } from 'lit';
import { directive, Directive, type DirectiveParameters, type PartInfo, PartType } from 'lit/directive.js';
import { getBinderNode } from './BinderNode.js';
import {
  _fromString,
  ArrayModel as BinderArrayModel,
  BooleanModel as BinderBooleanModel,
  hasFromString,
  NumberModel as BinderNumberModel,
  ObjectModel as BinderObjectModel,
  StringModel as BinderStringModel,
} from './Models.js';
import type { ProvisionalModel } from './ProvisionalModel.js';
import { getStringConverter } from './stringConverters.js';
import type { ValueError } from './Validation.js';
import { _validity, defaultValidity } from './Validity.js';

export interface FieldBase<T> {
  required: boolean;
  invalid: boolean;
  errorMessage: string;
  value: T | undefined;
}

/**
 * Subset of the HTML constraint validation API with the `checkValidity()` method.
 */
export type FieldConstraintValidation = Readonly<{
  validity: ValidityState;
  checkValidity(): boolean;
}>;

export type FieldElement<T = unknown> = FieldBase<T> & HTMLElement & Partial<FieldConstraintValidation>;

const props = ['required', 'invalid', 'errorMessage', 'value', 'validity', 'checkValidity'];

export function isFieldElement<T>(element: HTMLElement): element is FieldElement<T> {
  return props.some((prop) => prop in element);
}

interface FieldElementHolder<T> {
  get element(): FieldElement<T>;

  /**
   * @param element - the new element value
   * @deprecated will be read-only in future
   */
  set element(element: FieldElement<T>);
}

interface Field<T> extends FieldBase<T> {
  readonly model?: ProvisionalModel<T>;
}

interface FieldState<T> extends Field<T>, FieldElementHolder<T> {
  name: string;
  validity: ValidityState;
  strategy: FieldStrategy<T>;
}

type EventHandler = (event: Event) => void;

export type FieldStrategy<T = any> = Field<T> &
  FieldConstraintValidation & {
    onChange?: EventHandler;
    onInput?: EventHandler;
    removeEventListeners(): void;
  };

export abstract class AbstractFieldStrategy<T = any, E extends FieldElement<T> = FieldElement<T>>
  implements FieldStrategy<T>
{
  abstract required: boolean;

  abstract invalid: boolean;

  readonly model?: ProvisionalModel<T>;

  #element: E;

  /**
   * @privateRemarks
   * Fallback for missing .validity property API in Vaadin components.
   */
  #validityFallback: ValidityState = defaultValidity;

  readonly #eventHandlers = new Map<string, EventHandler>();

  constructor(element: E, model?: ProvisionalModel<T>) {
    this.#element = element;
    this.model = model;
  }

  get element(): E {
    return this.#element;
  }

  /**
   * @param element - the new element value
   * @deprecated will be read-only in future
   */
  set element(element: E) {
    this.#element = element;
  }

  get value(): T | undefined {
    return this.#element.value;
  }

  set value(value: T | undefined) {
    if (
      this.model instanceof BinderStringModel ||
      this.model === StringModel ||
      this.model instanceof StringModel ||
      this.model instanceof BinderNumberModel ||
      this.model === NumberModel ||
      this.model instanceof NumberModel
    ) {
      this.#element.value = value ?? ('' as T);
      return;
    }
    this.#element.value = value;
  }

  set errorMessage(_: string) {}

  get validity(): ValidityState {
    return this.#element.validity ?? this.#validityFallback;
  }

  get onChange(): EventHandler | undefined {
    return this.#eventHandlers.get('change');
  }

  set onChange(onChange: EventHandler | undefined) {
    this.#setEventHandler('change', onChange);
  }

  get onInput(): EventHandler | undefined {
    return this.#getEventHandler('input');
  }

  set onInput(onInput: EventHandler | undefined) {
    this.#setEventHandler('input', onInput);
  }

  checkValidity(): boolean {
    if (!this.#element.checkValidity) {
      return true;
    }

    const valid = this.#element.checkValidity();
    this.#validityFallback = {
      ...defaultValidity,
      valid,
      ...(valid ? {} : this.#detectValidityError()),
    };
    return valid;
  }

  setAttribute(key: string, val: any): void {
    if (val) {
      this.#element.setAttribute(key, '');
    } else {
      this.#element.removeAttribute(key);
    }
  }

  removeEventListeners(): void {
    for (const [type, handler] of this.#eventHandlers) {
      this.element.removeEventListener(type, handler);
      this.#eventHandlers.delete(type);
    }
  }

  #getEventHandler(type: string): EventHandler | undefined {
    return this.#eventHandlers.get(type);
  }

  #setEventHandler(type: string, handler?: EventHandler) {
    if (this.#eventHandlers.has(type)) {
      this.element.removeEventListener(type, this.#eventHandlers.get(type)!);
    }

    if (handler) {
      this.element.addEventListener(type, handler);
      this.#eventHandlers.set(type, handler);
    } else {
      this.#eventHandlers.delete(type);
    }
  }

  #detectValidityError(): Readonly<Partial<ValidityState>> {
    if (!('inputElement' in this.#element)) {
      // Not a Vaadin component field
      return { customError: true };
    }

    const inputElement = this.#element.inputElement as FieldElement<string>;

    if (this.#element.value === '') {
      if (inputElement.value === '') {
        return { valueMissing: true };
      }
      // Some value is entered, but not meaningful to the
      // web component — assume parse error.
      return { badInput: true };
    }
    // Unknown constraint violation
    return { customError: true };
  }
}

export class VaadinFieldStrategy<T = any, E extends FieldElement<T> = FieldElement<T>> extends AbstractFieldStrategy<
  T,
  E
> {
  #invalid = false;
  readonly #boundOnValidated = this.#onValidated.bind(this);
  readonly #boundOnUnparsableChange = this.#onUnparsableChange.bind(this);

  constructor(element: E, model?: ProvisionalModel<T>) {
    super(element, model);
    element.addEventListener('validated', this.#boundOnValidated);
    element.addEventListener('unparsable-change', this.#boundOnUnparsableChange);
  }

  set required(value: boolean) {
    this.element.required = value;
  }

  set invalid(value: boolean) {
    this.#invalid = value;
    this.element.invalid = value;
  }

  override set errorMessage(value: string) {
    this.element.errorMessage = value;
  }

  override removeEventListeners(): void {
    this.element.removeEventListener('validated', this.#boundOnValidated);
    this.element.removeEventListener('unparsable-change', this.#boundOnUnparsableChange);
  }

  #onValidated(e: Event): void {
    if (!(e instanceof CustomEvent) || typeof e.detail !== 'object') {
      return;
    }

    // Override built-in changes of the `invalid` flag in Vaadin components
    // to keep the `invalid` property state of the web component in sync.
    const invalid = !((e.detail ?? {}) as Partial<ValidityState>).valid;
    if (this.#invalid !== invalid) {
      this.element.invalid = this.#invalid;
    }

    // Some user interactions in Vaadin components do not dispatch `input`
    // event, such as validation upon closing the overlay, pressing Enter key.
    // One notable example is <vaadin-date-picker>. Use `validated` event in
    // addition to standard input events to handle those.
    this.onInput?.call(this.element, e);
  }

  #onUnparsableChange(e: Event) {
    this.onChange?.call(this.element, e);
  }

  override checkValidity(): boolean {
    // Ignore the `invalid` property of the Vaadin component to avoid
    // reading the component's internal old validation state and validate
    // the element based on the current state.
    const isElementInvalid = this.element.invalid;
    this.element.invalid = false;
    const valid = super.checkValidity();
    this.element.invalid = isElementInvalid;
    return valid;
  }
}

export class GenericFieldStrategy<T = any, E extends FieldElement<T> = FieldElement<T>> extends AbstractFieldStrategy<
  T,
  E
> {
  set required(value: boolean) {
    this.setAttribute('required', value);
  }

  set invalid(value: boolean) {
    this.setAttribute('invalid', value);
  }
}

type CheckedFieldElement<T> = FieldElement<T> & {
  checked: boolean;
};

export class CheckedFieldStrategy<
  T = any,
  E extends CheckedFieldElement<T> = CheckedFieldElement<T>,
> extends GenericFieldStrategy<T, E> {
  override get value(): T | undefined {
    if (this.model instanceof BinderBooleanModel || this.model === BooleanModel || this.model instanceof BooleanModel) {
      return this.element.checked as T;
    }

    return this.element.checked ? this.element.value : undefined;
  }

  override set value(val: T | undefined) {
    (this.element as { checked: boolean }).checked = /^(true|on)$/iu.test(String(val));
  }
}

export class CheckedGroupFieldStrategy<
  T = any,
  E extends FieldElement<T> = FieldElement<T>,
> extends GenericFieldStrategy<T, E> {
  override get value(): T | undefined {
    return super.value;
  }

  override set value(val: T | undefined) {
    super.value = val ?? ([] as T);
  }
}

type ComboBoxFieldElement<T> = FieldElement<T> & {
  value: string;
  selectedItem: T | null;
};

export class ComboBoxFieldStrategy<
  T,
  E extends ComboBoxFieldElement<T> = ComboBoxFieldElement<T>,
> extends VaadinFieldStrategy<T, E> {
  override get value(): T | undefined {
    if (
      this.model &&
      (this.model instanceof BinderObjectModel ||
        this.model === ObjectModel ||
        this.model instanceof ObjectModel ||
        this.model instanceof BinderArrayModel ||
        (this.model as Model) === ArrayModel ||
        this.model instanceof ArrayModel)
    ) {
      const { selectedItem } = this.element;
      return selectedItem ?? undefined;
    }

    return super.value;
  }

  override set value(val: T | undefined) {
    if (
      this.model instanceof BinderObjectModel ||
      this.model === ObjectModel ||
      this.model instanceof ObjectModel ||
      this.model instanceof BinderArrayModel ||
      (this.model as Model) === ArrayModel ||
      this.model instanceof ArrayModel
    ) {
      this.element.selectedItem = val ?? null;
    } else {
      super.value = val;
    }
  }
}

export class VaadinStringFieldStrategy extends VaadinFieldStrategy<string> {
  override get value(): string | undefined {
    return super.value;
  }

  override set value(val: string | undefined) {
    // Some Vaadin components (e.g. vaadin-time-picker) do not support setting
    // the value to `null` or `undefined`. Instead, set it to an empty string.
    super.value = val ?? '';
  }
}
function isEmptyObject(val: any): boolean {
  return val && typeof val === 'object' && !Array.isArray(val) && Object.keys(val).length === 0;
}

export class VaadinDateTimeFieldStrategy<
  T = string,
  E extends FieldElement<T> = FieldElement<T>,
> extends VaadinFieldStrategy<T, E> {
  override get value(): T | undefined {
    return super.value;
  }

  override set value(val: T | undefined) {
    const timestamp = Date.parse(val as string);

    if (!val || isEmptyObject(val) || Number.isNaN(timestamp)) {
      super.value = '' as T;
      return;
    }

    const date = new Date(timestamp);
    // Convert to ISO 8601 local combined date and time representation
    const tzOffsetMs = 60 * 1000 * date.getTimezoneOffset();
    super.value = new Date(timestamp - tzOffsetMs).toISOString().slice(0, 19) as T;
  }
}

type MultiSelectComboBoxFieldElement<T> = FieldElement<T> & {
  value: never;
  selectedItems: T;
};

export class MultiSelectComboBoxFieldStrategy<
  T,
  E extends MultiSelectComboBoxFieldElement<T> = MultiSelectComboBoxFieldElement<T>,
> extends VaadinFieldStrategy<T, E> {
  override get value(): T {
    return this.element.selectedItems;
  }

  override set value(val: any) {
    this.element.selectedItems = val;
  }
}

type SelectedFieldElement<T> = FieldElement<T> & {
  value: never;
  selected: T;
};

export class SelectedFieldStrategy<
  T,
  E extends SelectedFieldElement<T> = SelectedFieldElement<T>,
> extends GenericFieldStrategy<T, E> {
  override get value(): T {
    return this.element.selected;
  }

  override set value(val: T) {
    this.element.selected = val;
  }
}

type MaybeVaadinElementConstructor = {
  readonly version?: string;
};

export function getDefaultFieldStrategy<T>(
  elm: FieldElement<T>,
  model?: ProvisionalModel<T>,
): AbstractFieldStrategy<T> {
  switch (elm.localName) {
    case 'vaadin-checkbox':
    case 'vaadin-radio-button':
      return new CheckedFieldStrategy(elm as CheckedFieldElement<T>, model);
    case 'vaadin-checkbox-group':
      return new CheckedGroupFieldStrategy(elm, model);
    case 'vaadin-combo-box':
      return new ComboBoxFieldStrategy(elm as ComboBoxFieldElement<T>, model);
    case 'vaadin-list-box':
      return new SelectedFieldStrategy(elm as SelectedFieldElement<T>, model);
    case 'vaadin-multi-select-combo-box':
      return new MultiSelectComboBoxFieldStrategy(elm as MultiSelectComboBoxFieldElement<T>, model);
    case 'vaadin-rich-text-editor':
      return new GenericFieldStrategy(elm, model);
    case 'vaadin-time-picker':
      return new VaadinStringFieldStrategy(
        elm as FieldElement<string>,
        model as ProvisionalModel<string>,
      ) as AbstractFieldStrategy<T>;
    case 'vaadin-date-time-picker':
      return new VaadinDateTimeFieldStrategy(elm, model) as AbstractFieldStrategy<T>;
    default:
      if (elm.localName === 'input' && /^(checkbox|radio)$/u.test((elm as unknown as HTMLInputElement).type)) {
        return new CheckedFieldStrategy(elm as CheckedFieldElement<T>, model);
      }
      if ((elm.constructor as unknown as MaybeVaadinElementConstructor).version) {
        return new VaadinFieldStrategy(elm, model);
      }
      return new GenericFieldStrategy(elm, model);
  }
}

function convertFieldValue<T extends ProvisionalModel>(model: T, fieldValue: unknown) {
  if (typeof fieldValue !== 'string') {
    return fieldValue;
  }

  const stringConverter = getStringConverter(model);
  if (stringConverter) {
    return stringConverter.fromString(fieldValue);
  }

  if (hasFromString(model)) {
    return model[_fromString](fieldValue);
  }

  return fieldValue;
}

/**
 * Binds a form field component into a model.
 *
 * Example usage:
 *
 * ```
 * <vaadin-text-field ...="${field(model.name)}">
 * </vaadin-text-field>
 * ```
 */
export const field = directive(
  class extends Directive {
    fieldState?: FieldState<any>;

    constructor(partInfo: PartInfo) {
      super(partInfo);
      if (partInfo.type !== PartType.PROPERTY && partInfo.type !== PartType.ELEMENT) {
        throw new Error('Use as "<element {field(...)}" or <element ...={field(...)}"');
      }
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    override render(_model: ProvisionalModel<any>, _effect?: (element: Element) => void) {
      return nothing;
    }

    override update(part: ElementPart | PropertyPart, [model, effect]: DirectiveParameters<this>) {
      const element = part.element as FieldElement & HTMLInputElement;

      const binderNode = getBinderNode(model);

      if (!this.fieldState) {
        const fieldState = {
          errorMessage: '',
          name: '',
          value: '',
          required: false,
          invalid: false,
          model,
          validity: defaultValidity,
          element,
          strategy: binderNode.binder.getFieldStrategy(element, model),
        };

        this.fieldState = fieldState;

        const inputHandler = () => {
          fieldState.strategy.checkValidity();
          // When bad input is detected, skip reading new value in binder state
          if (!fieldState.strategy.validity.badInput) {
            fieldState.value = fieldState.strategy.value;
          }
          fieldState.validity = fieldState.strategy.validity;
          binderNode[_validity] = fieldState.validity;
          binderNode.value = convertFieldValue(model, fieldState.value);
          if (effect !== undefined) {
            effect.call(element, element);
          }
        };

        fieldState.strategy.onInput = inputHandler;
        fieldState.strategy.onChange = () => {
          inputHandler();
          void binderNode.validate();
        };

        const blurHandler = () => {
          inputHandler();
          void binderNode.validate();
          binderNode.visited = true;
        };

        element.addEventListener('blur', blurHandler);
      }

      const { fieldState } = this;

      if (fieldState.element !== element || fieldState.model !== model) {
        const { onInput } = fieldState.strategy;
        fieldState.strategy = binderNode.binder.getFieldStrategy(element, model);
        fieldState.strategy.onInput = onInput;
      }

      const { name } = binderNode;
      if (name !== fieldState.name) {
        fieldState.name = name;
        element.setAttribute('name', name);
      }

      const { value } = binderNode;
      const valueFromField = convertFieldValue(model, fieldState.value);
      if (value !== valueFromField && !(Number.isNaN(value) && Number.isNaN(valueFromField))) {
        const nonNanValue = Number.isNaN(value) ? '' : value;
        fieldState.value = nonNanValue;
        fieldState.strategy.value = nonNanValue;
      }

      const { required } = binderNode;
      if (required !== fieldState.required) {
        fieldState.required = required;
        fieldState.strategy.required = required;
      }

      const firstError: ValueError<any> | undefined = binderNode.ownErrors[0];
      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      const errorMessage = firstError?.message || '';
      if (errorMessage !== fieldState.errorMessage) {
        fieldState.errorMessage = errorMessage;
        fieldState.strategy.errorMessage = errorMessage;
      }

      const { invalid } = binderNode;
      if (invalid !== fieldState.invalid) {
        fieldState.invalid = invalid;
        fieldState.strategy.invalid = invalid;
      }

      return noChange;
    }
  },
);
