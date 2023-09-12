import Pageable from './types/Pageable';

export interface CrudService<T> {
  list: {
    (request: Pageable): Promise<T[]>;
  };
}
