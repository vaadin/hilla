import { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement, GridColumnProps } from '@hilla/react-components/GridColumn.js';
import { AutoGridBooleanRenderer, AutoGridNumberRenderer } from './autogrid-renderers';
import type { PropertyInfo } from './utils';

type ColumnOptions = Omit<GridColumnProps<any>, 'dangerouslySetInnerHTML'>;

export type RendererOptions<TItem> = {
  item: TItem;
  model: GridItemModel<TItem>;
  original: GridColumnElement<TItem>;
};

export function getColumnProps<TItem>(propertyInfo: PropertyInfo): ColumnOptions {
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
