import { createContext } from 'react';

export interface AutoCrudContextType<TItem> {
  refreshGrid: () => void;
}

export const AutoCrudContext = createContext<AutoCrudContextType<any> | undefined>(undefined);
