import { type ElementPart, noChange, nothing, type PropertyPart } from 'lit';
import { directive, Directive, type DirectiveParameters, type PartInfo, PartType } from 'lit/directive.js';
import { _fromString, AbstractModel, ArrayModel, ObjectModel, getBinderNode, hasFromString } from './Models.js';
import { _validity, defaultValidity } from './Validity.js';

interface FieldBase<T> {
  required: boolean;
  invalid: boolean;
  errorMessage: string;
  value: T;
}

/**
 * Subset of the HTML constraint validation API with the `checkValidity()` method.
 */
type FieldConstraintValidation = Readonly<{
  validity: ValidityState;
  checkValidity: () => boolean;
}>;

type FieldElement<T> = Element & FieldBase<T> & Partial<FieldConstraintValidation>;

interface FieldElementHolder<T> {
  get element(): FieldElement<T>;

  /**
   * @param element the new element value
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

export type FieldStrategy<T = any> = Field<T> & FieldConstraintValidation;

export abstract class AbstractFieldStrategy<T = any> implements FieldStrategy<T> {
  public abstract required: boolean;

  public abstract invalid: boolean;

  private _element: FieldElement<T>;

  /**
   * Fallback for missing .validity property API in Vaadin components.
   * @private
   */
  private _validityFallback: ValidityState = defaultValidity;

  public constructor(element: FieldElement<T>, public readonly model?: AbstractModel<T>) {
    this._element = element;
  }

  public get element() {
    return this._element;
  }

  /**
   * @param element the new element value
   * @deprecated will be read-only in future
   */
  public set element(element: FieldElement<T>) {
    this._element = element;
  }

  public get value() {
    return this.element.value;
  }

  public set value(value) {
    this.element.value = value;
  }

  public set errorMessage(_: string) {} // eslint-disable-line @typescript-eslint/no-empty-function

  public setAttribute(key: string, val: any) {
    if (val) {
      this.element.setAttribute(key, '');
    } else {
      this.element.removeAttribute(key);
    }
  }

  public get validity() {
    return this.element.validity || this._validityFallback;
  }

  public checkValidity() {
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

export class VaadinFieldStrategy<T = any> extends AbstractFieldStrategy<T> {
  private _invalid = false;

  constructor(element: FieldElement<T>, model?: AbstractModel<T>) {
    super(element, model);

    // Override built-in changes of the `invalid` flag in Vaadin components
    // to keep the `invalid` property state of the web component in sync.
    (element as any).addEventListener('validated', this._overrideVaadinInvalidChange.bind(this));
  }

  public set required(value: boolean) {
    this.element.required = value;
  }

  public set invalid(value: boolean) {
    this._invalid = value;
    this.element.invalid = value;
  }

  public override set errorMessage(value: string) {
    this.element.errorMessage = value;
  }

  private _overrideVaadinInvalidChange(e: CustomEvent<Partial<ValidityState>>) {
    if (this._invalid !== !e.detail.valid) {
      this.element.invalid = this._invalid;
    }
  }
}

export class GenericFieldStrategy extends AbstractFieldStrategy {
  public set required(value: boolean) {
    this.setAttribute('required', value);
  }

  public set invalid(value: boolean) {
    this.setAttribute('invalid', value);
  }
}

export class CheckedFieldStrategy extends GenericFieldStrategy {
  public override set value(val: any) {
    (this.element as any).checked = /^(true|on)$/i.test(String(val));
  }

  public override get value() {
    return (this.element as any).checked;
  }
}

export class ComboBoxFieldStrategy extends VaadinFieldStrategy {
  public override get value() {
    if (this.model && (this.model instanceof ObjectModel || this.model instanceof ArrayModel)) {
      const { selectedItem } = this.element as any;
      return selectedItem === null ? undefined : selectedItem;
    }

    return super.value;
  }

  public override set value(val: any) {
    if (this.model instanceof ObjectModel || this.model instanceof ArrayModel) {
      (this.element as any).selectedItem = val === undefined ? null : val;
    } else {
      super.value = val;
    }
  }
}

export class MultiSelectComboBoxFieldStrategy extends VaadinFieldStrategy {
  public override get value() {
    return (this.element as any).selectedItems;
  }

  public override set value(val: any) {
    (this.element as any).selectedItems = val;
  }
}

export class SelectedFieldStrategy extends GenericFieldStrategy {
  public override set value(val: any) {
    (this.element as any).selected = val;
  }

  public override get value() {
    return (this.element as any).selected;
  }
}

export function getDefaultFieldStrategy<T>(elm: any, model?: AbstractModel<T>): AbstractFieldStrategy<T> {
  switch (elm.localName) {
    case 'vaadin-checkbox':
    case 'vaadin-radio-button':
      return new CheckedFieldStrategy(elm, model);
    case 'vaadin-combo-box':
      return new ComboBoxFieldStrategy(elm, model);
    case 'vaadin-list-box':
      return new SelectedFieldStrategy(elm, model);
    case 'vaadin-multi-select-combo-box':
      return new MultiSelectComboBoxFieldStrategy(elm, model);
    case 'vaadin-rich-text-editor':
      return new GenericFieldStrategy(elm, model);
    default:
      if (elm.localName === 'input' && /^(checkbox|radio)$/.test(elm.type)) {
        return new CheckedFieldStrategy(elm, model);
      }
      return elm.constructor.version ? new VaadinFieldStrategy(elm, model) : new GenericFieldStrategy(elm, model);
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
    public fieldState?: FieldState<any>;

    public constructor(partInfo: PartInfo) {
      super(partInfo);
      if (partInfo.type !== PartType.PROPERTY && partInfo.type !== PartType.ELEMENT) {
        throw new Error('Use as "<element {field(...)}" or <element ...={field(...)}"');
      }
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public render(model: AbstractModel<any>, effect?: (element: Element) => void) {
      return nothing;
    }

    public override update(part: PropertyPart | ElementPart, [model, effect]: DirectiveParameters<this>) {
      const element = part.element as HTMLInputElement & FieldElement<any>;

      const binderNode = getBinderNode(model);

      if (!this.fieldState) {
        const fieldState = {
          name: '',
          value: '',
          required: false,
          invalid: false,
          errorMessage: '',
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

      const firstError = binderNode.ownErrors ? binderNode.ownErrors[0] : undefined;
      const errorMessage = (firstError && firstError.message) || '';
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
