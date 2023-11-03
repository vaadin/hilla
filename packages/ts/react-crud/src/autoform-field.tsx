import { _enum, type EnumModel } from '@hilla/form';
import { Checkbox, type CheckboxProps } from '@hilla/react-components/Checkbox.js';
import { DatePicker, type DatePickerProps } from '@hilla/react-components/DatePicker.js';
import { IntegerField, type IntegerFieldProps } from '@hilla/react-components/IntegerField.js';
import { NumberField, type NumberFieldProps } from '@hilla/react-components/NumberField.js';
import { Select, type SelectProps } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldProps } from '@hilla/react-components/TextField.js';
import { TimePicker, type TimePickerProps } from '@hilla/react-components/TimePicker.js';
import type { FieldDirectiveResult, UseFormResult } from '@hilla/react-form';
import type { JSX } from 'react';
import { useDatePickerI18n } from './locale.js';
import type { PropertyInfo } from './property-info.js';
import { convertToTitleCase } from './util.js';

export type SharedFieldProps = Readonly<{
  propertyInfo: PropertyInfo;
  colSpan?: number;
  form: UseFormResult<any>;
  options?: FieldOptions;
}>;

export type FieldOptions = Readonly<{
  label?: string;
  renderer?(props: { field: FieldDirectiveResult; label: string }): JSX.Element;
}>;

function getPropertyModel(form: UseFormResult<any>, propertyInfo: PropertyInfo) {
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return form.model[propertyInfo.name];
}

type AutoFormTextFieldProps = SharedFieldProps & TextFieldProps;

function AutoFormTextField({ propertyInfo, form, options, label, ...other }: AutoFormTextFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the typing issue
  return <TextField {...other} {...form.field(model)} label={label} />;
}

type AutoFormIntegerFieldProps = IntegerFieldProps & SharedFieldProps;

function AutoFormIntegerField({ propertyInfo, form, label, ...other }: AutoFormIntegerFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <IntegerField {...other} {...form.field(model)} label={label} />;
}

type AutoFormNumberFieldProps = NumberFieldProps & SharedFieldProps;

function AutoFormDecimalField({ propertyInfo, form, label, ...other }: AutoFormNumberFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <NumberField {...other} {...form.field(model)} label={label} />;
}

type AutoFormDateFieldProps = DatePickerProps & SharedFieldProps;

function AutoFormDateField({ propertyInfo, form, label, ...other }: AutoFormDateFieldProps) {
  const i18n = useDatePickerI18n();
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <DatePicker i18n={i18n} {...other} {...form.field(model)} label={label} />;
}

type AutoFormTimeFieldProps = SharedFieldProps & TimePickerProps;

function AutoFormTimeField({ propertyInfo, form, label, ...other }: AutoFormTimeFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <TimePicker {...other} {...form.field(model)} label={label} />;
}

type AutoFormEnumFieldProps = SelectProps & SharedFieldProps;

function AutoFormEnumField({ propertyInfo, form, label, ...other }: AutoFormEnumFieldProps) {
  const model = getPropertyModel(form, propertyInfo) as EnumModel;
  const options = Object.keys(model[_enum]).map((value) => ({
    label: convertToTitleCase(value),
    value,
  }));
  return <Select {...other} {...form.field(model)} label={label} items={options} />;
}

type AutoFormBooleanFieldProps = CheckboxProps & SharedFieldProps;

function AutoFormBooleanField({ propertyInfo, form, label, ...other }: AutoFormBooleanFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <Checkbox {...other} {...form.field(model)} label={label} />;
}

export type AutoFormFieldProps = CheckboxProps &
  DatePickerProps &
  IntegerFieldProps &
  NumberFieldProps &
  SelectProps &
  SharedFieldProps &
  TextFieldProps &
  TimePickerProps;

export function AutoFormField(props: AutoFormFieldProps): JSX.Element | null {
  const { form, propertyInfo, options } = props;
  const label = options?.label ?? propertyInfo.humanReadableName;
  if (options?.renderer) {
    return options.renderer({ field: form.field(getPropertyModel(form, propertyInfo)), label });
  }
  switch (props.propertyInfo.type) {
    case 'string':
      return <AutoFormTextField {...props} label={label}></AutoFormTextField>;
    case 'integer':
      return <AutoFormIntegerField {...props} label={label}></AutoFormIntegerField>;
    case 'decimal':
      return <AutoFormDecimalField {...props} label={label}></AutoFormDecimalField>;
    case 'date':
      return <AutoFormDateField {...props} label={label}></AutoFormDateField>;
    case 'time':
      return <AutoFormTimeField {...props} label={label}></AutoFormTimeField>;
    case 'enum':
      return <AutoFormEnumField {...props} label={label}></AutoFormEnumField>;
    case 'boolean':
      return <AutoFormBooleanField {...props} label={label}></AutoFormBooleanField>;
    default:
      return null;
  }
}
