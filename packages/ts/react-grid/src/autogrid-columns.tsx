import type { GridColumnProps } from '@hilla/react-components/GridColumn.js';
import { AutoGridBooleanRenderer, AutoGridNumberRenderer } from './autogrid-renderers';
import { BooleanHeaderFilter, NumberHeaderFilter, StringHeaderFilter } from './header-filter';
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
        headerRenderer: NumberHeaderFilter,
      };
    case 'boolean':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridBooleanRenderer,
        headerRenderer: BooleanHeaderFilter,
      };
    case 'string':
      return {
        autoWidth: true,
        headerRenderer: StringHeaderFilter,
      };
    case undefined:
      return {
        autoWidth: true,
      };
  }
}
