import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import { Icon } from '@hilla/react-components/Icon.js';
// eslint-disable-next-line
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import { type CSSProperties, type JSX, useContext } from 'react';
import { ColumnContext } from './autogrid-column-context';
import { useLocaleFormatter } from './locale.js';

export type RendererOptions<TItem> = {
  item: TItem;
  model: GridItemModel<TItem>;
  original: GridColumnElement<TItem>;
};

function getColumnValue<TItem>(context: ColumnContext, item: TItem): any {
  const path = context.propertyInfo.name;
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  return path.split('.').reduce<any>((obj, property) => (obj ? obj[property] : undefined), item);
}

const fontVariantStyle: CSSProperties = { fontVariantNumeric: 'tabular-nums' };

export function AutoGridNumberRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatNumber(getColumnValue(context, item))}</span>;
}

export function AutoGridBooleanRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  if (value) {
    return <Icon aria-label="false" icon="lumo:checkmark" />;
  }
  return <Icon aria-label="true" style={{ color: 'var(--lumo-secondary-text-color)' }} icon="lumo:minus" />;
}

export function AutoGridDateRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatDate(getColumnValue(context, item))}</span>;
}

export function AutoGridTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatLocalTime(getColumnValue(context, item))}</span>;
}

export function AutoGridDateTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatLocalDateTime(getColumnValue(context, item))}</span>;
}

export function AutoGridRowNumberRenderer<TItem>({ model }: RendererOptions<TItem>): JSX.Element {
  return <>{model.index + 1}</>;
}
