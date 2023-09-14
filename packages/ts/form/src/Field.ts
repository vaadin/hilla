/* eslint-disable accessor-pairs,sort-keys */
import { type ElementPart, noChange, nothing, type PropertyPart } from 'lit';
import { directive, Directive, type DirectiveParameters, type PartInfo, PartType } from 'lit/directive.js';
import {
  _fromString,
  type AbstractModel,
  ArrayModel,
  BooleanModel,
  ObjectModel,
  getBinderNode,
  hasFromString,
} from './Models.js';
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

export type FieldElement<T> = FieldBase<T> & HTMLElement & Partial<FieldConstraintValidation>;

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
  readonly model?: AbstractModel<T>;
}

interface FieldState<T> extends Field<T>, FieldElementHolder<T> {
  name: string;
  validity: ValidityState;
  strategy: FieldStrategy<T>;
}

export type FieldStrategy<T = any> = Field<T> &
  FieldConstraintValidation & {
    removeEventListeners(): void;
  };

export abstract class AbstractFieldStrategy<T = any, E extends FieldElement<T> = FieldElement<T>>
  implements FieldStrategy<T>
{
  abstract required: boolean;

  abstract invalid: boolean;

  readonly model?: AbstractModel<T>;

  private _element: E;

  /**
   * @privateRemarks
   * Fallback for missing .validity property API in Vaadin components.
   */
  private _validityFallback: ValidityState = defaultValidity;

  constructor(element: E, model?: AbstractModel<T>) {
    this._element = element;
    this.model = model;
  }

  get element(): E {
    return this._element;
  }

  /**
   * @param element - the new element value
   * @deprecated will be read-only in future
   */
  set element(element: E) {
    this._element = element;
  }

  get value(): T | undefined {
    return this.element.value;
  }

  set value(value: T | undefined) {
    this.element.value = value;
  }

  set errorMessage(_: string) {} // eslint-disable-line @typescript-eslint/no-empty-function

  get validity(): ValidityState {
    return this.element.validity ?? this._validityFallback;
  }

  checkValidity(): boolean {
    if (!this.element.checkValidity) {
      return true;
    }

    const valid = this.element.checkValidity();
    this._validityFallback = {
      ...defaultValidity,
      valid,
      ...(valid ? {} : this._detectValidityError()),
    };
    return valid;
  }

  setAttribute(key: string, val: any): void {
    if (val) {
      this.element.setAttribute(key, '');
    } else {
      this.element.removeAttribute(key);
    }
  }

  removeEventListeners(): void {}

  private _detectValidityError(): Readonly<Partial<ValidityState>> {
    if (!('inputElement' in this.element)) {
      // Not a Vaadin component field
      return { customError: true };
    }

    const inputElement = this.element.inputElement as FieldElement<string>;

    if (this.element.value === '') {
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
  private _invalid = false;
  private readonly _boundOnValidated = this._onValidated.bind(this);

  constructor(element: E, model?: AbstractModel<T>) {
    super(element, model);

    (element as EventTarget).addEventListener('validated', this._boundOnValidated);
  }

  set required(value: boolean) {
    this.element.required = value;
  }

  set invalid(value: boolean) {
    this._invalid = value;
    this.element.invalid = value;
  }

  override set errorMessage(value: string) {
    this.element.errorMessage = value;
  }

  override removeEventListeners(): void {
    this.element.removeEventListener('validated', this._boundOnValidated);
  }

  private _onValidated(e: Event): void {
    if (!(e instanceof CustomEvent) || typeof e.detail !== 'object') {
      return;
    }

    // Override built-in changes of the `invalid` flag in Vaadin components
    // to keep the `invalid` property state of the web component in sync.
    const invalid = !(e.detail satisfies Partial<ValidityState> as Partial<ValidityState>).valid;
    if (this._invalid !== invalid) {
      this.element.invalid = this._invalid;
    }
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
    if (this.model instanceof BooleanModel) {
      return this.element.checked as T;
    }

    return this.element.checked ? this.element.value : undefined;
  }

  override set value(val: T | undefined) {
    (this.element as { checked: boolean }).checked = /^(true|on)$/iu.test(String(val));
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
    if (this.model && (this.model instanceof ObjectModel || this.model instanceof ArrayModel)) {
      const { selectedItem } = this.element;
      return (selectedItem === null ? undefined : selectedItem) as T;
    }

    return super.value;
  }

  override set value(val: T | undefined) {
    if (this.model instanceof ObjectModel || this.model instanceof ArrayModel) {
      this.element.selectedItem = val === undefined ? null : val;
    } else {
      super.value = val;
    }
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

export function getDefaultFieldStrategy<T>(elm: FieldElement<T>, model?: AbstractModel<T>): AbstractFieldStrategy<T> {
  switch (elm.localName) {
    case 'vaadin-checkbox':
    case 'vaadin-radio-button':
      return new CheckedFieldStrategy(elm as CheckedFieldElement<T>, model);
    case 'vaadin-combo-box':
      return new ComboBoxFieldStrategy(elm as ComboBoxFieldElement<T>, model);
    case 'vaadin-list-box':
      return new SelectedFieldStrategy(elm as SelectedFieldElement<T>, model);
    case 'vaadin-multi-select-combo-box':
      return new MultiSelectComboBoxFieldStrategy(elm as MultiSelectComboBoxFieldElement<T>, model);
    case 'vaadin-rich-text-editor':
      return new GenericFieldStrategy(elm, model);
    default:
      if (elm.localName === 'input' && /^(checkbox|radio)$/u.test((elm as unknown as HTMLInputElement).type)) {
        return new CheckedFieldStrategy(elm as CheckedFieldElement<T>, model);
      }
      return (elm.constructor as unknown as MaybeVaadinElementConstructor).version
        ? new VaadinFieldStrategy(elm, model)
        : new GenericFieldStrategy(elm, model);
  }
}

function convertFieldValue<T extends AbstractModel<unknown>>(model: T, fieldValue: unknown) {
  return typeof fieldValue === 'string' && hasFromString(model) ? model[_fromString](fieldValue) : fieldValue;
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
    override render(_model: AbstractModel<any>, _effect?: (element: Element) => void) {
      return nothing;
    }

    override update(part: ElementPart | PropertyPart, [model, effect]: DirectiveParameters<this>) {
      const element = part.element as FieldElement<any> & HTMLInputElement;

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

        const updateValueFromElement = () => {
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

        element.addEventListener('input', updateValueFromElement);

        const changeBlurHandler = () => {
          updateValueFromElement();
          binderNode.visited = true;
        };

        element.addEventListener('blur', changeBlurHandler);
        element.addEventListener('change', changeBlurHandler);
      }

      const { fieldState } = this;

      if (fieldState.element !== element || fieldState.model !== model) {
        fieldState.strategy = binderNode.binder.getFieldStrategy(element, model);
      }

      const { name } = binderNode;
      if (name !== fieldState.name) {
        fieldState.name = name;
        element.setAttribute('name', name);
      }

      const { value } = binderNode;
      const valueFromField = convertFieldValue(model, fieldState.value);
      if (value !== valueFromField && !(Number.isNaN(value) && Number.isNaN(valueFromField))) {
        fieldState.value = value;
        fieldState.strategy.value = value;
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
