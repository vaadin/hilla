import type Pageable from './types/dev/hilla/mappedtypes/Pageable';

export interface CrudService<T> {
  list(request: Pageable): Promise<T[]>;
}
