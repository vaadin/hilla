package dev.hilla.crud;

import java.lang.reflect.Type;
import java.util.List;

import com.googlecode.gentyref.GenericTypeReflector;

import dev.hilla.EndpointExposed;
import dev.hilla.Nullable;
import dev.hilla.crud.filter.Filter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 */
@EndpointExposed
public class CrudRepositoryService<T, ID, R extends CrudRepository<T, ID> & JpaSpecificationExecutor<T>>
        implements CrudService<T, ID> {

    @Autowired
    private JpaFilterConverter jpaFilterConverter;

    @Autowired
    private ApplicationContext applicationContext;

    private R repository;
    private final Class<T> entityClass;

    public CrudRepositoryService() {
        this.entityClass = resolveEntityClass();
    }

    /**
     * Creates the service using the given repository.
     *
     * @param repository
     *            the JPA repository
     */
    public CrudRepositoryService(R repository) {
        this.repository = repository;
        this.entityClass = resolveEntityClass();
    }

    @PostConstruct
    private void init() {
        if (repository == null) {
            repository = resolveRepository();
        }
    }

    /**
     * Accessor for the repository instance.
     *
     * @return the repository instance
     */
    protected R getRepository() {
        return repository;
    }

    @Override
    public List<T> list(Pageable pageable, @Nullable Filter filter) {
        Specification<T> spec = jpaFilterConverter.toSpec(filter, entityClass);
        return getRepository().findAll(spec, pageable).getContent();
    }

    @Override
    public @Nullable T save(T value) {
        return getRepository().save(value);
    }

    @Override
    public void delete(ID id) {
        getRepository().deleteById(id);
    }

    @SuppressWarnings("unchecked")
    private R resolveRepository() {
        var repositoryTypeParam = CrudRepositoryService.class
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

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        var entityTypeParam = CrudRepositoryService.class
                .getTypeParameters()[0];
        Type entityType = GenericTypeReflector.getTypeParameter(getClass(),
                entityTypeParam);
        if (entityType == null) {
            throw new IllegalStateException(String.format(
                    "Unable to detect the type for the class '%s' in the "
                            + "class '%s'.",
                    entityTypeParam, getClass()));
        }
        return (Class<T>) GenericTypeReflector.erase(entityType);
    }
}
