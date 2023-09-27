import { useContext } from 'react';
import { ColumnContext } from './header-column-context';
import { GridItemModel } from '@hilla/react-components/Grid.js';
import { GridColumnElement } from '@hilla/react-components/GridColumn.js';

type RendererOptions<TItem> = {
  item: TItem;
  model: GridItemModel<TItem>;
  original: GridColumnElement<TItem>;
};
export function AutoGridColumn<TItem>({ item, model, original }: RendererOptions<TItem>) {
  const context = useContext(ColumnContext)!;
  const value = (item as any)[context.propertyInfo.name];
  if (context?.propertyInfo.modelType === 'number') {
    const formatted = new Intl.NumberFormat(undefined, {
      maximumFractionDigits: 0,
    }).format(value);
    return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
  } else {
    return <>{value}</>;
  }
}
