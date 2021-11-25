import { ElementPart, noChange, nothing, PropertyPart } from 'lit';
import { directive, Directive, DirectiveParameters, PartInfo, PartType } from 'lit/directive.js';
import { _fromString, AbstractModel, getBinderNode } from './Models.js';

interface Field {
  required: boolean;
  invalid: boolean;
  errorMessage: string;
  value: any;
}
interface FieldState extends Field {
  name: string;
  strategy: FieldStrategy;
}
export interface FieldStrategy extends Field {
  element: Element;
}

export abstract class AbstractFieldStrategy implements FieldStrategy {
  public abstract required: boolean;

  public abstract invalid: boolean;

  public element: Element & Field;

  public constructor(element: Element & Field) {
    this.element = element;
  }

  public validate = async () => [];

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
}

export class VaadinFieldStrategy extends AbstractFieldStrategy {
  public set required(value: boolean) {
    this.element.required = value;
  }

  public set invalid(value: boolean) {
    this.element.invalid = value;
  }

  public override set errorMessage(value: string) {
    this.element.errorMessage = value;
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
    const { selectedItem } = this.element as any;
    return selectedItem === null ? undefined : selectedItem;
  }

  public override set value(val: any) {
    (this.element as any).selectedItem = val === undefined ? null : val;
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

export function getDefaultFieldStrategy(elm: any): FieldStrategy {
  switch (elm.localName) {
    case 'vaadin-checkbox':
    case 'vaadin-radio-button':
      return new CheckedFieldStrategy(elm);
    case 'vaadin-combo-box':
      return new ComboBoxFieldStrategy(elm);
    case 'vaadin-list-box':
      return new SelectedFieldStrategy(elm);
    case 'vaadin-rich-text-editor':
      return new GenericFieldStrategy(elm);
    default:
      if (elm.localName === 'input' && /^(checkbox|radio)$/.test(elm.type)) {
        return new CheckedFieldStrategy(elm);
      }
      return elm.constructor.version ? new VaadinFieldStrategy(elm) : new GenericFieldStrategy(elm);
  }
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
    public fieldState?: FieldState;

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
      const element = part.element as HTMLInputElement & Field;

      const binderNode = getBinderNode(model);
      const fieldStrategy = binderNode.binder.getFieldStrategy(element);

      const convertFieldValue = (fieldValue: any) => {
        const fromString = (model as any)[_fromString];
        return typeof fieldValue === 'string' && fromString ? fromString(fieldValue) : fieldValue;
      };

      if (!this.fieldState) {
        this.fieldState = {
          name: '',
          value: '',
          required: false,
          invalid: false,
          errorMessage: '',
          strategy: fieldStrategy,
        };

        const { fieldState } = this;

        const updateValueFromElement = () => {
          fieldState.value = fieldState.strategy.value;
          binderNode.value = convertFieldValue(fieldState.value);
          if (effect !== undefined) {
            effect.call(element, element);
          }
        };

        element.oninput = () => {
          updateValueFromElement();
        };

        const changeBlurHandler = () => {
          updateValueFromElement();
          binderNode.visited = true;
        };
        element.onblur = changeBlurHandler;
        element.onchange = changeBlurHandler;

        element.checkValidity = () => !fieldState.invalid;
      }

      const { fieldState } = this;
      const { name } = binderNode;
      if (name !== fieldState.name) {
        fieldState.name = name;
        element.setAttribute('name', name);
      }

      const { value } = binderNode;
      const valueFromField = convertFieldValue(fieldState.value);
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
