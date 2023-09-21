import { TextField } from '@hilla/react-components/TextField.js';
import type { PropertyInfo } from './utils';

export function createFilterField(prop: PropertyInfo, additionalProps: Record<string, any>): JSX.Element | null {
  let field: JSX.Element | null;
  let commonProps = {};
  commonProps = { ...commonProps, ...additionalProps };
  if (prop.modelType === 'string') {
    field = <TextField placeholder="Filter..." {...commonProps}></TextField>;
  } else {
    field = null;
  }

  return field;
}
