package com.vaadin.hilla.crud;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.hilla.EndpointExposed;
import com.vaadin.hilla.crud.filter.Filter;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * A browser-callable service that delegates list operations to a JPA
 * repository.
 */
@EndpointExposed
public class ListRepositoryService<T, ID, R extends CrudRepository<T, ID> & JpaSpecificationExecutor<T>>
        implements ListService<T>, GetService<T, ID>, CountService {

    // https://github.com/spring-projects/spring-boot/blob/1d35deaaf02cca9af84fdaceddf5335149db0aec/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/data/web/SpringDataWebProperties.java#L84
    public static final int DEFAULT_PAGE_SIZE_LIMIT = 2000;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    SpringDataWebProperties springDataWebProperties;

    private R repository;
    private final Class<T> entityClass;

    /*
     * Creates the service by autodetecting the type of repository and entity to
     * use from the generics.
     */
    public ListRepositoryService() {
        this.entityClass = resolveEntityClass();
    }

    /**
     * Creates the service using the given repository.
     *
     * @param repository
     *            the JPA repository
     */
    public ListRepositoryService(R repository) {
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
        var limitedPageable = limitSize(pageable);
        Specification<T> spec = toSpec(filter);
        return getRepository().findAll(spec, limitedPageable).getContent();
    }

    private PageRequest limitSize(Pageable pageable) {
        int maxPageSize = Optional.ofNullable(springDataWebProperties)
                .map(SpringDataWebProperties::getPageable)
                .map(SpringDataWebProperties.Pageable::getMaxPageSize)
                .orElse(DEFAULT_PAGE_SIZE_LIMIT);

        var effectivePageSize = pageable.isPaged()
                ? Math.min(pageable.getPageSize(), maxPageSize)
                : maxPageSize;

        return PageRequest.of(pageable.isPaged() ? pageable.getPageNumber() : 0,
                effectivePageSize, pageable.getSort());
    }

    @Override
    public Optional<T> get(ID id) {
        return getRepository().findById(id);
    }

    @Override
    public boolean exists(ID id) {
        return getRepository().existsById(id);
    }

    /**
     * Counts the number of entities that match the given filter.
     *
     * @param filter
     *            the filter, or {@code null} to use no filter
     * @return
     */
    @Override
    public long count(@Nullable Filter filter) {
        return getRepository().count(toSpec(filter));
    }

    /**
     * Converts the given filter to a JPA specification.
     *
     * @param filter
     *            the filter to convert
     * @return a JPA specification
     */
    protected Specification<T> toSpec(@Nullable Filter filter) {
        return JpaFilterConverter.toSpec(filter, entityClass);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    protected Class<T> resolveEntityClass() {
        var entityTypeParam = ListRepositoryService.class
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
