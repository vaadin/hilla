import Pageable from './types/Pageable';

export interface CrudEndpoint<T> {
  list: {
    (request: Pageable): Promise<T[]>;
  };
}
