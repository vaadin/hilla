import type { GridSorterDirection } from '@hilla/react-components/GridSorter.js';
import { createContext } from 'react';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import type { PropertyInfo } from './utils';

export type HeaderColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
  sortDirection: GridSorterDirection;
};

export const HeaderColumnContext = createContext<HeaderColumnContext | null>(null);
