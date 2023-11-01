import { _enum, type EnumModel } from '@hilla/form';
import { Checkbox, type CheckboxProps } from '@hilla/react-components/Checkbox.js';
import { DatePicker, type DatePickerProps } from '@hilla/react-components/DatePicker.js';
import { IntegerField, type IntegerFieldProps } from '@hilla/react-components/IntegerField.js';
import { NumberField, type NumberFieldProps } from '@hilla/react-components/NumberField.js';
import { Select, type SelectProps } from '@hilla/react-components/Select.js';
import { TextField, type TextFieldProps } from '@hilla/react-components/TextField.js';
import { TimePicker, type TimePickerProps } from '@hilla/react-components/TimePicker.js';
import type { UseFormResult } from '@hilla/react-form';
import type { JSX } from 'react';
import { useDatePickerI18n } from './locale.js';
import type { PropertyInfo } from './property-info.js';
import { convertToTitleCase } from './util.js';

export type SharedFieldProps = Readonly<{
  propertyInfo: PropertyInfo;
  colSpan?: number;
  form: UseFormResult<any>;
}>;

function getPropertyModel(form: UseFormResult<any>, propertyInfo: PropertyInfo) {
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return form.model[propertyInfo.name];
}

type AutoFormTextFieldProps = SharedFieldProps & TextFieldProps;

function AutoFormTextField({ propertyInfo, form, ...other }: AutoFormTextFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the typing issue
  return <TextField {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

type AutoFormIntegerFieldProps = IntegerFieldProps & SharedFieldProps;

function AutoFormIntegerField({ propertyInfo, form, ...other }: AutoFormIntegerFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <IntegerField {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

type AutoFormNumberFieldProps = NumberFieldProps & SharedFieldProps;

function AutoFormDecimalField({ propertyInfo, form, ...other }: AutoFormNumberFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <NumberField {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

type AutoFormDateFieldProps = DatePickerProps & SharedFieldProps;

function AutoFormDateField({ propertyInfo, form, ...other }: AutoFormDateFieldProps) {
  const i18n = useDatePickerI18n();
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <DatePicker i18n={i18n} {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

type AutoFormTimeFieldProps = SharedFieldProps & TimePickerProps;

function AutoFormTimeField({ propertyInfo, form, ...other }: AutoFormTimeFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <TimePicker {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

type AutoFormEnumFieldProps = SelectProps & SharedFieldProps;

function AutoFormEnumField({ propertyInfo, form, ...other }: AutoFormEnumFieldProps) {
  const model = getPropertyModel(form, propertyInfo) as EnumModel;
  const options = Object.keys(model[_enum]).map((value) => ({
    label: convertToTitleCase(value),
    value,
  }));
  return <Select {...other} {...form.field(model)} label={propertyInfo.humanReadableName} items={options} />;
}

type AutoFormBooleanFieldProps = CheckboxProps & SharedFieldProps;

function AutoFormBooleanField({ propertyInfo, form, ...other }: AutoFormBooleanFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  // @ts-expect-error: TODO: fix the dangerouslySetInnerHTML typing issue
  return <Checkbox {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
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
  switch (props.propertyInfo.type) {
    case 'string':
      return <AutoFormTextField {...props}></AutoFormTextField>;
    case 'integer':
      return <AutoFormIntegerField {...props}></AutoFormIntegerField>;
    case 'decimal':
      return <AutoFormDecimalField {...props}></AutoFormDecimalField>;
    case 'date':
      return <AutoFormDateField {...props}></AutoFormDateField>;
    case 'time':
      return <AutoFormTimeField {...props}></AutoFormTimeField>;
    case 'enum':
      return <AutoFormEnumField {...props}></AutoFormEnumField>;
    case 'boolean':
      return <AutoFormBooleanField {...props}></AutoFormBooleanField>;
    default:
      return null;
  }
}
