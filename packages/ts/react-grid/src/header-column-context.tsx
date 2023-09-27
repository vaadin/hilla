import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import { type Dispatch, type SetStateAction, createContext } from 'react';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import type { PropertyInfo } from './utils';

export interface SortState {
  path: string;
  direction: GridSorterDirection;
}

export type HeaderColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
  sortState: SortState | null;
  setSortState: Dispatch<SetStateAction<SortState | null>>;
};

export const HeaderColumnContext = createContext<HeaderColumnContext | null>(null);
