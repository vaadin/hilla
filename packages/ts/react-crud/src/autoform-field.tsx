import { TextField, type TextFieldProps } from '@hilla/react-components/TextField.js';
import type { UseFormResult } from '@hilla/react-form';
import type { JSX } from 'react';
import type { PropertyInfo } from './property-info.js';

type AutoFormFieldProps = Omit<TextFieldProps, 'dangerouslySetInnerHTML' | 'disabled' | 'label'> &
  Readonly<{
    propertyInfo: PropertyInfo;
    form: UseFormResult<any>;
    disabled?: boolean;
  }>;

export function AutoFormField({ propertyInfo, form, disabled, ...other }: AutoFormFieldProps): JSX.Element {
  if (propertyInfo.type === 'string' || propertyInfo.type === 'number') {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const model = form.model[propertyInfo.name];
    return <TextField disabled={disabled} {...other} {...form.field(model)} label={propertyInfo.humanReadableName} />;
  }
  return <></>;
}
