import { createContext } from 'react';
import type PropertyStringFilter from './types/dev/hilla/crud/filter/PropertyStringFilter';
import type { PropertyInfo } from './utils';

export type HeaderColumnContext = {
  propertyInfo: PropertyInfo;
  setPropertyFilter(propertyFilter: PropertyStringFilter): void;
};

export const HeaderColumnContext = createContext<HeaderColumnContext | null>(null);
