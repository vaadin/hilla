import type { GridColumnProps } from '@hilla/react-components/GridColumn.js';
import { AutoGridBooleanRenderer, AutoGridNumberRenderer } from './autogrid-renderers';
import type { PropertyInfo } from './utils';

type ColumnOptions = Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;

// eslint-disable-next-line consistent-return
export function getColumnProps(propertyInfo: PropertyInfo): ColumnOptions {
  // eslint-disable-next-line default-case
  switch (propertyInfo.modelType) {
    case 'number':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridNumberRenderer,
      };
    case 'boolean':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridBooleanRenderer,
      };
    case 'string':
      return {
        autoWidth: true,
      };
    case undefined:
      return {
        autoWidth: true,
      };
  }
}
