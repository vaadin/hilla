import { createContext } from 'react';
import type { CrudService } from './crud.js';
import type { PropertyInfo } from './property-info.js';

export interface AutoCrudContextType<TItem> {
  noDelete?: boolean;

  editItem(itemToEdit: TItem): void;

  deleteItem(itemToDelete: TItem): void;
}

export const AutoCrudContext = createContext<AutoCrudContextType<any> | undefined>(undefined);
