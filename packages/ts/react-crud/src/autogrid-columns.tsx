import type { GridColumnProps } from '@hilla/react-components/GridColumn.js';
import { type JSX, useContext } from 'react';
import { ColumnContext, CustomColumnContext } from './autogrid-column-context';
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
  NoHeaderFilter,
  NumberHeaderFilter,
  StringHeaderFilter,
  TimeHeaderFilter,
  type HeaderFilterProps,
  type HeaderRendererProps,
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
  const HeaderFilterRenderer =
    customColumnOptions?.filterable === false ? NoHeaderFilter : typeColumnOptions.headerFilterRenderer;
  // TODO: Remove eslint-disable when all TypeScript version issues are resolved
  // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
  const columnOptions: ColumnOptions = customColumnOptions
    ? { ...typeColumnOptions, headerFilterRenderer: HeaderFilterRenderer, ...customColumnOptions }
    : typeColumnOptions;
  if (!columnOptions.headerFilterRenderer) {
    console.error(`No filter renderer defined for column ${propertyInfo.name}`);
  }
  return columnOptions;
}

export function InternalHeaderFilterRenderer({ original }: HeaderRendererProps): JSX.Element | null {
  const { setPropertyFilter, headerFilterRenderer: HeaderFilterRenderer } = useContext(ColumnContext)!;
  if (HeaderFilterRenderer) {
    return <HeaderFilterRenderer original={original} setPropertyFilter={setPropertyFilter} />;
  }
  return null;
}

export function InternalCustomHeaderFilterRenderer({ original }: HeaderRendererProps): JSX.Element | null {
  const { setPropertyFilter, headerFilterRenderer: HeaderFilterRenderer } = useContext(CustomColumnContext)!;
  if (HeaderFilterRenderer) {
    return <HeaderFilterRenderer original={original} setPropertyFilter={setPropertyFilter} />;
  }
  return null;
}
