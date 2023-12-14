import type { GridItemModel } from '@hilla/react-components/Grid.js';
import type { GridColumnElement } from '@hilla/react-components/GridColumn.js';
import { Icon } from '@hilla/react-components/Icon.js';
import { type ComponentType, createContext, type CSSProperties, type JSX, useContext } from 'react';
import { ColumnContext } from './autogrid-column-context';
import type { ItemCounts } from './data-provider';
import { useLocaleFormatter } from './locale.js';
import { convertToTitleCase } from './util';

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

const fontVariantStyle: CSSProperties = { fontVariantNumeric: 'tabular-nums' };

export function AutoGridIntegerRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatInteger(getColumnValue(context, item))}</span>;
}

export function AutoGridDecimalRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const formatter = useLocaleFormatter();
  const context = useContext(ColumnContext)!;
  return <span style={fontVariantStyle}>{formatter.formatDecimal(getColumnValue(context, item))}</span>;
}

export function AutoGridEnumRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item) || '';
  return <span>{convertToTitleCase(value)}</span>;
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

export function AutoGridJsonRenderer<TItem>({ item }: RendererOptions<TItem>): JSX.Element {
  const context = useContext(ColumnContext)!;
  const value = getColumnValue(context, item);
  const jsonString = value ? JSON.stringify(value) : '';
  const jsonPreview = jsonString.length > 50 ? `${jsonString.substring(0, 50)}...` : jsonString;
  return <span>{jsonPreview}</span>;
}

export function AutoGridRowNumberRenderer<TItem>({ model }: RendererOptions<TItem>): JSX.Element {
  return <>{model.index + 1}</>;
}

export type FooterContextType = {
  totalCount?: boolean;
  filteredCount?: boolean;
  footerCountRenderer?: ComponentType<ItemCounts>;
  itemCounts?: ItemCounts;
};

export const FooterContext = createContext<FooterContextType>(undefined!);

export function AutoGridFooterItemCountRenderer(): JSX.Element {
  const footerContext = useContext(FooterContext);
  const { totalCount, filteredCount, itemCounts, footerCountRenderer: FooterRenderer } = footerContext;

  if (FooterRenderer) {
    return <FooterRenderer {...itemCounts} />;
  }

  let filterCountText: string | undefined;
  if (filteredCount && itemCounts?.filteredCount !== undefined) {
    filterCountText =
      totalCount && itemCounts.totalCount !== undefined
        ? `Showing: ${itemCounts.filteredCount} out of ${itemCounts.totalCount}`
        : `Showing: ${itemCounts.filteredCount}`;
  } else if (totalCount && itemCounts?.totalCount !== undefined) {
    filterCountText = `Total: ${itemCounts.totalCount}`;
  }
  if (filterCountText) {
    return <p>{filterCountText}</p>;
  }
  return <></>;
}
