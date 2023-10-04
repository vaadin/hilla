package dev.hilla.crud;

import dev.hilla.EndpointExposed;
import dev.hilla.Nullable;
import dev.hilla.crud.filter.Filter;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 */
@EndpointExposed
public class CrudRepositoryService<T, ID, R extends CrudRepository<T, ID> & JpaSpecificationExecutor<T>>
        extends ListRepositoryService<T, ID, R> implements CrudService<T, ID> {

    /*
     * Creates the service by autodetecting the type of repository and entity to
     * use from the generics.
     */
    public CrudRepositoryService() {
        super();
    }

    /**
     * Creates the service using the given repository.
     *
     * @param repository
     *            the JPA repository
     */
    public CrudRepositoryService(R repository) {
        super(repository);
    }

    @Override
    public @Nullable T save(T value) {
        return getRepository().save(value);
    }

    @Override
    public T get(ID id) {
        return getRepository().findById(id).orElse(null);
    }

    @Override
    public boolean exists(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    public void delete(ID id) {
        getRepository().deleteById(id);
    }

    /**
     * Counts the number of entities that match the given filter.
     *
     * @param filter
     *            the filter, or {@code null} to use no filter
     * @return
     */
    public long count(@Nullable Filter filter) {
        return getRepository().count(toSpec(filter));
    }

}
