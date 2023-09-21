package dev.hilla.crud;

import java.lang.reflect.ParameterizedType;
import java.util.List;

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

    /**
     * Creates the service using the given repository.
     */
    public CrudRepositoryService() {
        this.entityClass = resolveEntityClass();
    }

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
        Class<R> repositoryClass = (Class<R>) resolveGenericRuntimeClass(2);
        return applicationContext.getBean(repositoryClass);
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        return (Class<T>) resolveGenericRuntimeClass(0);
    }

    private Class<?> resolveGenericRuntimeClass(int argIndex) {
        Class<?> clazz = getClass();
        while (!clazz.getSuperclass().equals(CrudRepositoryService.class)) {
            clazz = clazz.getSuperclass();
        }
        return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass())
                .getActualTypeArguments()[argIndex];
    }

}
