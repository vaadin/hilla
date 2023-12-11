import { _enum, type AbstractModel, type EnumModel, type Validator } from '@hilla/form';
import { Checkbox } from '@hilla/react-components/Checkbox.js';
import { DatePicker } from '@hilla/react-components/DatePicker.js';
import { DateTimePicker } from '@hilla/react-components/DateTimePicker.js';
import { IntegerField } from '@hilla/react-components/IntegerField.js';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { Select } from '@hilla/react-components/Select.js';
import { TextArea } from '@hilla/react-components/TextArea.js';
import { TextField } from '@hilla/react-components/TextField.js';
import { TimePicker } from '@hilla/react-components/TimePicker.js';
import type { FieldDirectiveResult, UseFormResult } from '@hilla/react-form';
import { useFormPart } from '@hilla/react-form';
import {
  cloneElement,
  type ComponentType,
  createElement,
  type CSSProperties,
  type JSX,
  useEffect,
  useMemo,
} from 'react';
import { useDatePickerI18n, useDateTimePickerI18n } from './locale.js';
import type { PropertyInfo } from './model-info.js';
import { convertToTitleCase } from './util.js';

export type AutoFormFieldProps = Readonly<{
  propertyInfo: PropertyInfo;
  form: UseFormResult<any>;
  options: FieldOptions;
  disabled?: boolean;
}>;

type CustomFormFieldProps = FieldDirectiveResult & Readonly<{ label?: string; disabled?: boolean }>;

export type FieldOptions = Readonly<{
  /**
   * The id to apply to the field.
   */
  id?: string;
  /**
   * The class names to add to the field.
   */
  className?: string;
  /**
   * The style to apply to the field.
   */
  style?: CSSProperties;
  /**
   * The label to show for the field. If not specified, a human-readable label
   * is generated from the property name.
   */
  label?: string;
  /**
   * The placeholder to when the field is empty.
   *
   * Note that some field types, such as checkbox, do not support a placeholder.
   */
  placeholder?: string;
  /**
   * The helper text to display below the field.
   *
   * Note that some field types, such as checkbox, do not support a helper text.
   */
  helperText?: string;
  /**
   * The number of columns to span. This value is passed to the underlying
   * FormLayout, unless a custom layout is used. In that case, the value is
   * ignored.
   */
  colspan?: number;
  /**
   * Whether the field should be disabled.
   */
  disabled?: boolean;
  /**
   * Whether the field should be readonly.
   */
  readonly?: boolean;
  /**
   * The element to render for the field. This allows customizing field props
   * that are not supported by the field options, or to render a different field
   * component. Other field options are automatically applied to the element,
   * and the element is automatically bound to the form. If not specified, a
   * default field element is rendered based on the property type.
   *
   * The element must be a field component, such as TextField, TextArea,
   * NumberField, etc., otherwise form binding will not work. For more
   * sophisticated customizations, use the `renderer` option.
   *
   * If the field options also specify a renderer function, then the element is
   * ignored.
   *
   * Example enabling the clear button for a text field:
   * ```tsx
   * {
   *   element: <TextField clearButtonVisible />
   * }
   * ```
   *
   * Example rendering a text area instead of a text field:
   * ```tsx
   * {
   *   element: <TextArea />
   * }
   * ```
   */
  element?: JSX.Element;
  /**
   * Allows to specify a custom renderer for the field, for example to render a
   * custom type of field or apply an additional layout around the field. The
   * renderer receives field props that must be applied to the custom field
   * component in order to connect it to the form.
   *
   * In order to customize one of the default fields, or render a different type
   * of field, consider using the `element` option instead.
   *
   * Example:
   * ```tsx
   * {
   *   renderer: ({ field }) => (
   *     <div>
   *       <TextArea {...field} />
   *       <p>Number of words: {calculateNumberOfWords()}</p>
   *     </div>
   *   )
   * }
   * ```
   */
  renderer?(props: { field: CustomFormFieldProps }): JSX.Element;
  /**
   * Validators to apply to the field. The validators are added to the form
   * when the field is rendered.
   * UseMemo is recommended for the validators, so that they are not recreated
   * on every render.
   */
  validators?: Validator[];
}>;

type CommonFieldProps = Pick<
  FieldOptions,
  'className' | 'colspan' | 'disabled' | 'helperText' | 'id' | 'label' | 'placeholder' | 'readonly' | 'style'
>;

type FieldRendererProps = Readonly<{
  model: AbstractModel;
  field: FieldDirectiveResult;
  element?: JSX.Element;
  fieldProps: CommonFieldProps;
}>;

function getPropertyModel(form: UseFormResult<any>, propertyInfo: PropertyInfo) {
  const pathParts = propertyInfo.name.split('.');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return pathParts.reduce<any>((model, property) => (model ? model[property] : undefined), form.model);
}

function renderFieldElement(
  defaultComponentType: ComponentType,
  { element, field, fieldProps }: FieldRendererProps,
  additionalProps: any = {},
) {
  const fieldElement = element ?? createElement(defaultComponentType);
  return cloneElement(fieldElement, { ...fieldProps, ...additionalProps, ...fieldElement.props, ...field });
}

function AutoFormTextField(props: FieldRendererProps) {
  return renderFieldElement(TextField, props);
}

function AutoFormIntegerField(props: FieldRendererProps) {
  return renderFieldElement(IntegerField, props);
}

function AutoFormDecimalField(props: FieldRendererProps) {
  return renderFieldElement(NumberField, props);
}

function AutoFormDateField(props: FieldRendererProps) {
  const i18n = useDatePickerI18n();
  return renderFieldElement(DatePicker, props, { i18n });
}

function AutoFormTimeField(props: FieldRendererProps) {
  return renderFieldElement(TimePicker, props);
}

function AutoFormDateTimeField(props: FieldRendererProps) {
  const i18n = useDateTimePickerI18n();
  return renderFieldElement(DateTimePicker, props, { i18n });
}

function AutoFormEnumField(props: FieldRendererProps) {
  const enumModel = props.model as EnumModel;
  const items = Object.keys(enumModel[_enum]).map((value) => ({
    label: convertToTitleCase(value),
    value,
  }));
  return renderFieldElement(Select, props, { items });
}

function AutoFormBooleanField(props: FieldRendererProps) {
  return renderFieldElement(Checkbox, props);
}

function AutoFormObjectField({ model, fieldProps }: FieldRendererProps) {
  const part = useFormPart(model);
  const jsonString = part.value ? JSON.stringify(part.value) : '';
  return <TextArea {...fieldProps} value={jsonString} readonly />;
}

export function AutoFormField(props: AutoFormFieldProps): JSX.Element | null {
  const { form, propertyInfo, options } = props;
  const label = options.label ?? propertyInfo.humanReadableName;
  const model = getPropertyModel(form, propertyInfo);
  const field = form.field(model);

  const formPart = useFormPart(model);
  const defaultValidators = useMemo(() => formPart.validators, []);
  const { validators } = options;
  useEffect(() => {
    formPart.setValidators([...defaultValidators, ...(validators ?? [])]);
  }, [validators]);

  if (options.renderer) {
    const customFieldProps = { ...field, disabled: props.disabled, label };
    return options.renderer({ field: customFieldProps });
  }

  const fieldProps: CommonFieldProps = {
    id: options.id,
    className: options.className,
    style: options.style,
    label,
    placeholder: options.placeholder,
    helperText: options.helperText,
    colspan: options.colspan,
    disabled: options.disabled ?? props.disabled,
    readonly: options.readonly,
  };

  const rendererProps: FieldRendererProps = { model, field, element: options.element, fieldProps };

  switch (props.propertyInfo.type) {
    case 'string':
      return <AutoFormTextField {...rendererProps}></AutoFormTextField>;
    case 'integer':
      return <AutoFormIntegerField {...rendererProps}></AutoFormIntegerField>;
    case 'decimal':
      return <AutoFormDecimalField {...rendererProps}></AutoFormDecimalField>;
    case 'date':
      return <AutoFormDateField {...rendererProps}></AutoFormDateField>;
    case 'time':
      return <AutoFormTimeField {...rendererProps}></AutoFormTimeField>;
    case 'datetime':
      return <AutoFormDateTimeField {...rendererProps}></AutoFormDateTimeField>;
    case 'enum':
      return <AutoFormEnumField {...rendererProps}></AutoFormEnumField>;
    case 'boolean':
      return <AutoFormBooleanField {...rendererProps}></AutoFormBooleanField>;
    case 'object':
      return <AutoFormObjectField {...rendererProps}></AutoFormObjectField>;
    default:
      return null;
  }
}
