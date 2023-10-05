import type { GridColumnElement, GridColumnProps } from '@hilla/react-components/GridColumn.js';
import {
  AutoGridBooleanRenderer,
  AutoGridDateRenderer,
  AutoGridDateTimeRenderer,
  AutoGridNumberRenderer,
  AutoGridTimeRenderer,
} from './autogrid-renderers';
import { BooleanHeaderFilter, NoHeaderFilter, NumberHeaderFilter, StringHeaderFilter } from './header-filter';
import type { PropertyInfo } from './property-info';

type ColumnOptions = Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'> & {
  headerRenderer: React.ComponentType<Readonly<{ original: GridColumnElement }>>;
};

// eslint-disable-next-line consistent-return
export function getColumnProps(propertyInfo: PropertyInfo): ColumnOptions {
  // eslint-disable-next-line default-case
  switch (propertyInfo.type) {
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
    case 'date':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridDateRenderer,
        headerRenderer: NoHeaderFilter,
      };
    case 'time':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridTimeRenderer,
        headerRenderer: NoHeaderFilter,
      };
    case 'datetime':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridDateTimeRenderer,
        headerRenderer: NoHeaderFilter,
      };
    case 'string':
      return {
        autoWidth: true,
        headerRenderer: StringHeaderFilter,
      };
    case 'object':
    case undefined:
      return {
        autoWidth: true,
        headerRenderer: NoHeaderFilter,
      };
  }
}
