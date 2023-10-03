import { createContext } from 'react';
import { CrudService } from './crud';
import { PropertyInfo } from './property-info';

export interface AutoCrudContextType<TItem> {
  service: CrudService<TItem>;
  properties: PropertyInfo[];
  refreshGrid: () => void;
}

export const AutoCrudContext = createContext<AutoCrudContextType<any> | undefined>(undefined);
