import type { GridColumnProps } from '@hilla/react-components/GridColumn.js';
import { AutoGridNumberRenderer } from './autogrid-number-renderer';
import type { PropertyInfo } from './utils';

type ColumnOptions = Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;

export function getColumnProps<TItem>(propertyInfo: PropertyInfo): ColumnOptions {
  if (propertyInfo.modelType === 'number') {
    return {
      autoWidth: true,
      textAlign: 'end',
      flexGrow: 0,
      renderer: AutoGridNumberRenderer,
    };
  }
  return { autoWidth: true };
}
