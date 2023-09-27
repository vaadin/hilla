import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import { useContext } from 'react';
import { ColumnContext } from './header-column-context';

type RendererOptions<TItem> = {
  item: TItem;
  model: GridItemModel<TItem>;
  original: GridColumnElement<TItem>;
};

export function AutoGridNumberRenderer<TItem>({ item, model, original }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  const value = (item as any)[context.propertyInfo.name];
  const formatted = new Intl.NumberFormat(undefined, {
    maximumFractionDigits: 0,
  }).format(value);
  return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
}
