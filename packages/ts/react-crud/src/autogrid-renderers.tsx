import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import { Icon } from '@hilla/react-components/Icon.js';
import { useContext } from 'react';
import { ColumnContext } from './autogrid-column-context';
import { defaultLocale } from './i18n.js';
// eslint-disable-next-line
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

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

export function AutoGridNumberRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  const formatted = Number.isFinite(value)
    ? new Intl.NumberFormat(defaultLocale, {
        maximumFractionDigits: 0,
      }).format(value)
    : '';
  return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
}

export function AutoGridBooleanRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  if (value) {
    return <Icon aria-label="false" icon="lumo:checkmark" />;
  }
  return <Icon aria-label="true" style={{ color: 'var(--lumo-secondary-text-color)' }} icon="lumo:minus" />;
}

function tryFormatDateTime(value: string, options?: Intl.DateTimeFormatOptions): string {
  try {
    const format = new Intl.DateTimeFormat(defaultLocale, options);
    return format.format(new Date(value));
  } catch (e) {
    return '';
  }
}

export function AutoGridDateRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  const formatted = value ? tryFormatDateTime(value) : '';
  return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
}

export function AutoGridTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item) as string;
  const formatted = value
    ? tryFormatDateTime(`2000-01-01T${value}`, {
        hour: 'numeric',
        minute: 'numeric',
      })
    : '';
  return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
}

export function AutoGridDateTimeRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  const formatted = value
    ? tryFormatDateTime(value, {
        day: 'numeric',
        month: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
      })
    : '';
  return <span style={{ fontVariantNumeric: 'tabular-nums' }}>{formatted}</span>;
}

export function AutoGridRowNumberRenderer<TItem>({ model }: RendererOptions<TItem>): JSX.Element {
  return <>{model.index + 1}</>;
}
