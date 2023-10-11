import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import { type Dispatch, type SetStateAction, createContext } from 'react';
import type { PropertyInfo } from './property-info';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';

export interface SortState {
  path: string;
  direction: GridSorterDirection;
}

export type ColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
  sortState: SortState | null;
  setSortState: Dispatch<SetStateAction<SortState | null>>;
};

export const ColumnContext = createContext<ColumnContext | null>(null);
