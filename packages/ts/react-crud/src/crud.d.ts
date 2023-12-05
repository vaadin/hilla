import type FilterUnion from './types/dev/hilla/crud/filter/FilterUnion.js';
import type Pageable from './types/dev/hilla/mappedtypes/Pageable';

export interface FormService<T> {
  save(value: T): Promise<T | undefined>;
  delete(id: any): Promise<void>;
}

export interface ListService<T> {
  list(request: Pageable, filter: FilterUnion | undefined): Promise<T[]>;
}

export interface CrudService<T> extends FormService<T>, ListService<T> {}
