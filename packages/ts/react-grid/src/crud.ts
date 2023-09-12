import type Filter from './types/dev/hilla/crud/filter/Filter';
import type Pageable from './types/dev/hilla/mappedtypes/Pageable';

export interface CrudService<T> {
  list(request: Pageable, filter: Filter | undefined): Promise<T[]>;
}
