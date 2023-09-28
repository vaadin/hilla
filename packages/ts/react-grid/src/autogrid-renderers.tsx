import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import { Icon } from '@hilla/react-components/Icon.js';
import { useContext } from 'react';
import { ColumnContext } from './autogrid-column-context';
// eslint-disable-next-line
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

export type RendererOptions<TItem> = {
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

export function AutoGridBooleanRenderer<TItem>({ item, model, original }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  const value = (item as any)[context.propertyInfo.name];
  if (value) {
    return <Icon aria-label="false" icon="lumo:checkmark" />;
  }
  return <Icon aria-label="true" style={{ color: 'var(--lumo-secondary-text-color)' }} icon="lumo:minus" />;
}
