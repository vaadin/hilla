import type Filter from './types/dev/hilla/crud/filter/Filter';
import type Pageable from './types/dev/hilla/mappedtypes/Pageable';

export interface CrudService<T> extends ListService<T> {
  save(value: T): Promise<T | undefined>;
}

export interface ListService<T> {
  list(request: Pageable, filter: Filter | undefined): Promise<T[]>;
}
