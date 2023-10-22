import { IntegerField } from '@hilla/react-components/IntegerField';
import { NumberField } from '@hilla/react-components/NumberField.js';
import { TextField } from '@hilla/react-components/TextField.js';
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

export function AutoFormField(props: AutoFormFieldProps): JSX.Element | null {
  switch (props.propertyInfo.type) {
    case 'string':
      return <AutoFormTextField {...props}></AutoFormTextField>;
    case 'integer':
      return <AutoFormIntegerField {...props}></AutoFormIntegerField>;
    case 'decimal':
      return <AutoFormDecimalField {...props}></AutoFormDecimalField>;
    default:
      return null;
  }
}
