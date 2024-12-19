package com.vaadin.hilla.crud;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.Type;

import com.googlecode.gentyref.GenericTypeReflector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.vaadin.hilla.EndpointExposed;

/**
 * A browser-callable service that delegates list operations to a JPA
 * repository.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.jpa.ListRepositoryService}
 *             instead
 */
@Deprecated(forRemoval = true)
@EndpointExposed
public class ListRepositoryService<T, ID, R extends CrudRepository<T, ID> & JpaSpecificationExecutor<T>>
        extends
        com.vaadin.flow.spring.data.jpa.ListRepositoryService<T, ID, R> {

    @Autowired
    private ApplicationContext applicationContext;

    /*
     * Creates the service by autodetecting the type of repository and entity to
     * use from the generics.
     */
    public ListRepositoryService() {
        super(null);
    }

    /**
     * Creates the service using the given repository.
     *
     * @param repository
     *            the JPA repository
     */
    public ListRepositoryService(R repository) {
        super(repository);
    }

    @PostConstruct
    private void init() {
        if (getRepository() == null) {
            internalSetRepository(resolveRepository());
        }
    }

    private R resolveRepository() {
        var repositoryTypeParam = ListRepositoryService.class
                .getTypeParameters()[2];
        Type entityType = GenericTypeReflector.getTypeParameter(getClass(),
                repositoryTypeParam);
        if (entityType == null) {
            throw new IllegalStateException(String.format(
                    "Unable to detect the type for the class '%s' in the "
                            + "class '%s'.",
                    repositoryTypeParam, getClass()));
        }
        Class<R> repositoryClass = (Class<R>) GenericTypeReflector
                .erase(entityType);
        return applicationContext.getBean(repositoryClass);
    }

}
