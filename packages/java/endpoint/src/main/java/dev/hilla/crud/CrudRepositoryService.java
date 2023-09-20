package dev.hilla.crud;

import java.util.List;

import dev.hilla.EndpointExposed;
import dev.hilla.Nullable;
import dev.hilla.crud.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 */
@EndpointExposed
public class CrudRepositoryService<T, ID> implements CrudService<T> {

    @Autowired
    private JpaFilterConverter jpaFilterConverter;

    private JpaSpecificationExecutor<T> repository;
    private Class<T> entityClass;

    /**
     * Creates the service using the given repository.
     *
     * @param repository
     *            the JPA repository
     */
    public <R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> CrudRepositoryService(
            Class<T> entityClass, R repository) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    protected JpaSpecificationExecutor<T> getRepository() {
        return repository;
    }

    @Override
    public List<T> list(Pageable pageable, @Nullable Filter filter) {
        Specification<T> spec = jpaFilterConverter.toSpec(filter, entityClass);
        return repository.findAll(spec, pageable).getContent();
    }

}
