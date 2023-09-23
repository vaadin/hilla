import { TextField } from '@hilla/react-components/TextField.js';
import type { UseFormResult } from '@hilla/react-form';
import type { PropertyInfo } from './utils.js';

type AutoFormFieldProps = {
  propertyInfo: PropertyInfo;
  form: UseFormResult<any>;
  disabled?: boolean;
};
export function AutoFormField({ propertyInfo, form, disabled }: AutoFormFieldProps): JSX.Element {
  if (propertyInfo.modelType === 'string' || propertyInfo.modelType === 'number') {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const model = form.model[propertyInfo.name];
    return <TextField disabled={disabled} {...form.field(model)} label={propertyInfo.humanReadableName}></TextField>;
  }
  return <></>;
}
