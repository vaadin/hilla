import type { GridColumnProps } from '@hilla/react-components/GridColumn.js';
import {
  AutoGridBooleanRenderer,
  AutoGridDateRenderer,
  AutoGridDateTimeRenderer,
  AutoGridDecimalRenderer,
  AutoGridEnumRenderer,
  AutoGridIntegerRenderer,
  AutoGridJsonRenderer,
  AutoGridTimeRenderer,
} from './autogrid-renderers';
import {
  BooleanHeaderFilter,
  DateHeaderFilter,
  EnumHeaderFilter,
  type HeaderFilterProps,
  NoHeaderFilter,
  NumberHeaderFilter,
  StringHeaderFilter,
  TimeHeaderFilter,
} from './header-filter';
import type { PropertyInfo } from './model-info';

export type ColumnOptions = HeaderFilterProps & Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;

// eslint-disable-next-line consistent-return
function getTypeColumnOptions(propertyInfo: PropertyInfo): ColumnOptions {
  // eslint-disable-next-line default-case
  switch (propertyInfo.type) {
    case 'integer':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridIntegerRenderer,
        headerFilterRenderer: NumberHeaderFilter,
      };
    case 'decimal':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridDecimalRenderer,
        headerFilterRenderer: NumberHeaderFilter,
      };
    case 'boolean':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridBooleanRenderer,
        headerFilterRenderer: BooleanHeaderFilter,
      };
    case 'date':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridDateRenderer,
        headerFilterRenderer: DateHeaderFilter,
      };
    case 'time':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridTimeRenderer,
        headerFilterRenderer: TimeHeaderFilter,
      };
    case 'datetime':
      return {
        autoWidth: true,
        textAlign: 'end',
        flexGrow: 0,
        renderer: AutoGridDateTimeRenderer,
        headerFilterRenderer: DateHeaderFilter,
      };
    case 'enum':
      return {
        autoWidth: true,
        renderer: AutoGridEnumRenderer,
        headerFilterRenderer: EnumHeaderFilter,
      };
    case 'string':
      return {
        autoWidth: true,
        headerFilterRenderer: StringHeaderFilter,
      };
    case 'object':
      return {
        autoWidth: true,
        renderer: AutoGridJsonRenderer,
        headerFilterRenderer: NoHeaderFilter,
      };
    default:
      return {
        autoWidth: true,
        headerFilterRenderer: NoHeaderFilter,
      };
  }
}

export function getColumnOptions(
  propertyInfo: PropertyInfo,
  customColumnOptions: ColumnOptions | undefined,
): ColumnOptions {
  const typeColumnOptions = getTypeColumnOptions(propertyInfo);
  const headerFilterRenderer =
    customColumnOptions?.filterable === false
      ? NoHeaderFilter
      : typeColumnOptions.headerFilterRenderer ?? NoHeaderFilter;
  // TODO: Remove eslint-disable when all TypeScript version issues are resolved
  // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
  return customColumnOptions
    ? { ...typeColumnOptions, headerFilterRenderer, ...customColumnOptions }
    : typeColumnOptions;
}
