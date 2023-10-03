package dev.hilla.crud;

import dev.hilla.EndpointExposed;
import dev.hilla.Nullable;
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
    public void delete(ID id) {
        getRepository().deleteById(id);
    }

}
