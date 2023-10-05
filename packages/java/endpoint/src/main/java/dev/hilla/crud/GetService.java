package dev.hilla.crud;

/**
 * A browser-callable service that can fetch the given type of object.
 */
public interface GetService<T, ID> {

    /**
     * Gets the object with the given id.
     *
     * @param id
     *            the id of the object
     * @return the object, or null if no object was found
     */
    T get(ID id);

    /**
     * Checks if an object with the given id exists.
     *
     * @param id
     *            the id of the object
     * @return {@code true} if the object exists, {@code false} otherwise
     */
    default boolean exists(ID id) {
        return get(id) != null;
    }

}
