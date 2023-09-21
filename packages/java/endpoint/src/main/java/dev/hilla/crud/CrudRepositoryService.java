package dev.hilla.crud;


import java.lang.reflect.Type;
import java.util.List;

import com.googlecode.gentyref.GenericTypeReflector;

import dev.hilla.EndpointExposed;
import dev.hilla.Nullable;
import dev.hilla.crud.filter.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 */
@EndpointExposed
public class CrudRepositoryService<T, ID, R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>>
        implements CrudService<T> {

    @Autowired
    private JpaFilterConverter jpaFilterConverter;

    @Autowired
    private ApplicationContext applicationContext;

    private JpaSpecificationExecutor<T> repository;
    private final Class<T> entityClass;

    public CrudRepositoryService() {
        this.entityClass = resolveEntityClass();
    }

    /**
     * Initializes the repository instance if it hasn't been initialized yet,
     * otherwise returns the existing one.
     * <p>
     * This method uses ApplicationContext to obtain the Repository instance, so
     * it is not suitable for use in the constructor.
     *
     * @return the repository instance
     */
    protected JpaSpecificationExecutor<T> getRepository() {
        if (repository == null) {
            repository = resolveRepository();
        }
        return repository;
    }

    @Override
    public List<T> list(Pageable pageable, @Nullable Filter filter) {
        Specification<T> spec = jpaFilterConverter.toSpec(filter, entityClass);
        return getRepository().findAll(spec, pageable).getContent();
    }

    @SuppressWarnings("unchecked")
    private JpaSpecificationExecutor<T> resolveRepository() {
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
