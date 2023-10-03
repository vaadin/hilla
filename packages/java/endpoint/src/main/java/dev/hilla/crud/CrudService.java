package dev.hilla.crud;

import dev.hilla.Nullable;

/**
 * A browser-callable service that can create, read and update a given type of
 * object.
 */
public interface CrudService<T, ID> extends ListService<T> {

    /**
     * Saves the given object and returns the (potentially) updated object.
     * <p>
     * If you store the object in a SQL database, the returned object might have
     * a new id or updated consistency version.
     *
     * @param value
     *            the object to save
     * @return the fresh object or {@code null} if no object was found to update
     */
    @Nullable
    T save(T value);

    /**
     * Deletes the object with the given id.
     *
     * @param id
     *            the id of the object to delete
     */
    void delete(ID id);
}
