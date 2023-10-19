import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import { type Dispatch, type SetStateAction, createContext } from 'react';
import type { ColumnOptions } from './autogrid-columns';
import type { PropertyInfo } from './property-info';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';

export interface SorterState {
  direction: GridSorterDirection;
}

export type SortState = Record<string, SorterState | undefined>;

export type ColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
  sortState: SortState;
  setSortState: Dispatch<SetStateAction<SortState>>;
  customColumnOptions?: ColumnOptions;
};

export const ColumnContext = createContext<ColumnContext | null>(null);
