package dev.hilla.crud;

import java.util.List;

import dev.hilla.EndpointExposed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A browser-callable service that delegates crud operations to a JPA
 * repository.
 */
@EndpointExposed
public class CrudRepositoryService<T, ID> implements CrudService<T> {

    private JpaRepository<T, ID> repository;

    /**
     * Creates the service using the given repository.
     * 
     * @param repository
     *            the JPA repository
     */
    public CrudRepositoryService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> list(Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }

}
