import { type EnumModel, _enum } from '@hilla/form';
import { Checkbox } from '@hilla/react-components/Checkbox.js';
import { DatePicker } from '@hilla/react-components/DatePicker.js';
import { DateTimePicker } from '@hilla/react-components/DateTimePicker.js';
import { IntegerField } from '@hilla/react-components/IntegerField.js';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { Select } from '@hilla/react-components/Select.js';
import { TextField } from '@hilla/react-components/TextField.js';
import { TimePicker } from '@hilla/react-components/TimePicker.js';
import type { UseFormResult } from '@hilla/react-form';
import type { JSX } from 'react';
import type { PropertyInfo } from './property-info.js';

interface AutoFormFieldProps {
  propertyInfo: PropertyInfo;
  form: UseFormResult<any>;
  disabled?: boolean;
}

function getPropertyModel(form: UseFormResult<any>, propertyInfo: PropertyInfo) {
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return form.model[propertyInfo.name];
}

function AutoFormTextField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <TextField disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormIntegerField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <IntegerField disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormDecimalField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <NumberField disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormDateField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <DatePicker disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormTimeField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <TimePicker disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormDateTimeField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <DateTimePicker disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

function AutoFormEnumField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo) as EnumModel;
  const options = Object.keys(model[_enum]).map((value) => ({
    label: value,
    value,
  }));
  return <Select disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} items={options} />;
}

function AutoFormBooleanField({ propertyInfo, form, disabled }: AutoFormFieldProps) {
  const model = getPropertyModel(form, propertyInfo);
  return <Checkbox disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName} />;
}

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
    case 'datetime':
      return null; // waiting for new RC release <AutoFormDateTimeField {...props}></AutoFormDateTimeField>;
    case 'enum':
      return <AutoFormEnumField {...props}></AutoFormEnumField>;
    case 'boolean':
      return <AutoFormBooleanField {...props}></AutoFormBooleanField>;
    default:
      return null;
  }
}
