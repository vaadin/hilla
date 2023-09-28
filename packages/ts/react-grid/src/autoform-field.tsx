import { TextField } from '@hilla/react-components/TextField.js';
import type { UseFormResult } from '@hilla/react-form';
import type { PropertyInfo } from './utils.js';

type AutoFormFieldProps = {
  propertyInfo: PropertyInfo;
  form: UseFormResult<any>;
};
export function AutoFormField({ propertyInfo, form }: AutoFormFieldProps): JSX.Element {
  if (propertyInfo.type === 'string' || propertyInfo.type === 'number') {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const model = form.model[propertyInfo.name];
    return <TextField {...form.field(model)} label={propertyInfo.humanReadableName}></TextField>;
  }
  return <></>;
}
