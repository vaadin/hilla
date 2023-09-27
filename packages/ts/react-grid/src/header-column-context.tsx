import { createContext } from 'react';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import type { PropertyInfo } from './utils';

export type ColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
};

export const ColumnContext = createContext<ColumnContext | null>(null);
