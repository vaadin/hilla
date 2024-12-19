package com.vaadin.hilla.crud;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.vaadin.hilla.EndpointExposed;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.jpa.CrudRepositoryService}
 *             instead
 */
@Deprecated(forRemoval = true)
@EndpointExposed
public class CrudRepositoryService<T, ID, R extends CrudRepository<T, ID> & JpaSpecificationExecutor<T>>
        extends
        com.vaadin.flow.spring.data.jpa.CrudRepositoryService<T, ID, R> {

    /*
     * Creates the service by autodetecting the type of repository and entity to
     * use from the generics.
     */
    public CrudRepositoryService() {
        super(null);
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

}
