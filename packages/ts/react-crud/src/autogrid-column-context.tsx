import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import { type Dispatch, type SetStateAction, createContext, type ComponentType } from 'react';
import type { ColumnOptions } from './autogrid-columns';
import type { HeaderFilterRendererProps } from './header-filter';
import type { PropertyInfo } from './model-info';
import type FilterUnion from './types/dev/hilla/crud/filter/FilterUnion';

export interface SorterState {
  direction: GridSorterDirection;
}

export type SortState = Record<string, SorterState | undefined>;

export type ColumnContext = Readonly<{
  propertyInfo: PropertyInfo;
  setColumnFilter(filter: FilterUnion): void;
  sortState: SortState;
  setSortState: Dispatch<SetStateAction<SortState>>;
  customColumnOptions?: ColumnOptions;
  headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
}>;

export const ColumnContext = createContext<ColumnContext | null>(null);

export type CustomColumnContext = Readonly<{
  setColumnFilter(filter: FilterUnion): void;
  headerFilterRenderer: ComponentType<HeaderFilterRendererProps>;
}>;

export const CustomColumnContext = createContext<CustomColumnContext | null>(null);
