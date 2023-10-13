import { createContext } from 'react';
import type { CrudService } from './crud.js';
import type { PropertyInfo } from './property-info.js';

export interface AutoCrudContextType<TItem> {
  service: CrudService<TItem>;
  properties: PropertyInfo[];
  refreshGrid(): void;
}

export const AutoCrudContext = createContext<AutoCrudContextType<any> | undefined>(undefined);
