package dev.hilla.crud;

/**
 * A browser-callable service that can create, read, update, and delete a given
 * type of object.
 */
public interface CrudService<T, ID> extends ListService<T>, FormService<T, ID> {
}
