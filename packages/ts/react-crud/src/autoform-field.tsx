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
import type { CSSProperties, JSX } from 'react';
import { useEffect, useMemo } from 'react';
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
   * Allows to specify a custom renderer for the field, for example to render a
   * custom type of field or apply an additional layout around the field. The
   * renderer receives field props that must be applied to the custom field
   * component in order to connect it to the form.
   *
   * Example:
   * ```tsx
   * {
   *   renderer: ({ field }) => (
   *     <TextArea {...field} />
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
  fieldProps: CommonFieldProps;
}>;

function getPropertyModel(form: UseFormResult<any>, propertyInfo: PropertyInfo) {
  const pathParts = propertyInfo.name.split('.');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return pathParts.reduce<any>((model, property) => (model ? model[property] : undefined), form.model);
}

function AutoFormTextField({ field, fieldProps }: FieldRendererProps) {
  return <TextField {...field} {...fieldProps} />;
}

function AutoFormIntegerField({ field, fieldProps }: FieldRendererProps) {
  return <IntegerField {...field} {...fieldProps} />;
}

function AutoFormDecimalField({ field, fieldProps }: FieldRendererProps) {
  return <NumberField {...field} {...fieldProps} />;
}

function AutoFormDateField({ field, fieldProps }: FieldRendererProps) {
  const i18n = useDatePickerI18n();
  return <DatePicker i18n={i18n} {...field} {...fieldProps} />;
}

function AutoFormTimeField({ field, fieldProps }: FieldRendererProps) {
  return <TimePicker {...field} {...fieldProps} />;
}

function AutoFormDateTimeField({ field, fieldProps }: FieldRendererProps) {
  const i18n = useDateTimePickerI18n();
  return <DateTimePicker i18n={i18n} {...field} {...fieldProps} />;
}

function AutoFormEnumField({ model, field, fieldProps }: FieldRendererProps) {
  const enumModel = model as EnumModel;
  const options = Object.keys(enumModel[_enum]).map((value) => ({
    label: convertToTitleCase(value),
    value,
  }));
  return <Select {...field} {...fieldProps} items={options} />;
}

function AutoFormBooleanField({ field, fieldProps }: FieldRendererProps) {
  return <Checkbox {...field} {...fieldProps} />;
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

  switch (props.propertyInfo.type) {
    case 'string':
      return <AutoFormTextField model={model} field={field} fieldProps={fieldProps}></AutoFormTextField>;
    case 'integer':
      return <AutoFormIntegerField model={model} field={field} fieldProps={fieldProps}></AutoFormIntegerField>;
    case 'decimal':
      return <AutoFormDecimalField model={model} field={field} fieldProps={fieldProps}></AutoFormDecimalField>;
    case 'date':
      return <AutoFormDateField model={model} field={field} fieldProps={fieldProps}></AutoFormDateField>;
    case 'time':
      return <AutoFormTimeField model={model} field={field} fieldProps={fieldProps}></AutoFormTimeField>;
    case 'datetime':
      return <AutoFormDateTimeField model={model} field={field} fieldProps={fieldProps}></AutoFormDateTimeField>;
    case 'enum':
      return <AutoFormEnumField model={model} field={field} fieldProps={fieldProps}></AutoFormEnumField>;
    case 'boolean':
      return <AutoFormBooleanField model={model} field={field} fieldProps={fieldProps}></AutoFormBooleanField>;
    case 'object':
      return <AutoFormObjectField model={model} field={field} fieldProps={fieldProps}></AutoFormObjectField>;
    default:
      return null;
  }
}
