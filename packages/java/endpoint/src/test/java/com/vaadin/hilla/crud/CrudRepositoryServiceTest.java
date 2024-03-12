package com.vaadin.hilla.crud;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.EndpointController;
import com.vaadin.hilla.push.PushConfigurer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        CrudRepositoryServiceTest.DefaultJpaRepositoryService.class,
        CrudRepositoryServiceTest.CustomCrudRepository.class,
        CrudRepositoryServiceTest.CustomCrudRepositoryService.class,
        CrudRepositoryServiceTest.CustomJpaRepository.class,
        CrudRepositoryServiceTest.CustomJpaRepositoryService.class })
@ContextConfiguration(classes = { CrudConfiguration.class })
@EnableAutoConfiguration(exclude = { EndpointController.class,
        PushConfigurer.class })
public class CrudRepositoryServiceTest {

    @Repository
    static class CustomCrudRepository
            implements CrudRepository<TestObject, Integer>,
            JpaSpecificationExecutor<TestObject> {
        @Override
        public Optional<TestObject> findOne(Specification<TestObject> spec) {
            return Optional.empty();
        }

        @Override
        public List<TestObject> findAll(Specification<TestObject> spec) {
            return null;
        }

        @Override
        public Page<TestObject> findAll(Specification<TestObject> spec,
                Pageable pageable) {
            return null;
        }

        @Override
        public List<TestObject> findAll(Specification<TestObject> spec,
                Sort sort) {
            return null;
        }

        @Override
        public long count(Specification<TestObject> spec) {
            return 0;
        }

        @Override
        public boolean exists(Specification<TestObject> spec) {
            return false;
        }

        @Override
        public long delete(Specification<TestObject> spec) {
            return 0;
        }

        @Override
        public <S extends TestObject, R> R findBy(
                Specification<TestObject> spec,
                Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public <S extends TestObject> S save(S entity) {
            return null;
        }

        @Override
        public <S extends TestObject> Iterable<S> saveAll(
                Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<TestObject> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public Iterable<TestObject> findAll() {
            return null;
        }

        @Override
        public Iterable<TestObject> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {
        }

        @Override
        public void delete(TestObject entity) {
        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {
        }

        @Override
        public void deleteAll(Iterable<? extends TestObject> entities) {
        }

        @Override
        public void deleteAll() {
        }
    }

    @Repository
    static class CustomJpaRepository
            implements JpaRepository<TestObject, Integer>,
            JpaSpecificationExecutor<TestObject> {
        @Override
        public void flush() {
        }

        @Override
        public <S extends TestObject> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends TestObject> List<S> saveAllAndFlush(
                Iterable<S> entities) {
            return null;
        }

        @Override
        public void deleteAllInBatch(Iterable<TestObject> entities) {
        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Integer> integers) {
        }

        @Override
        public void deleteAllInBatch() {
        }

        @Override
        public TestObject getOne(Integer integer) {
            return null;
        }

        @Override
        public TestObject getById(Integer integer) {
            return null;
        }

        @Override
        public TestObject getReferenceById(Integer integer) {
            return null;
        }

        @Override
        public <S extends TestObject> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends TestObject> List<S> findAll(Example<S> example,
                Sort sort) {
            return null;
        }

        @Override
        public Optional<TestObject> findOne(Specification<TestObject> spec) {
            return Optional.empty();
        }

        @Override
        public List<TestObject> findAll(Specification<TestObject> spec) {
            return null;
        }

        @Override
        public Page<TestObject> findAll(Specification<TestObject> spec,
                Pageable pageable) {
            return null;
        }

        @Override
        public List<TestObject> findAll(Specification<TestObject> spec,
                Sort sort) {
            return null;
        }

        @Override
        public long count(Specification<TestObject> spec) {
            return 0;
        }

        @Override
        public boolean exists(Specification<TestObject> spec) {
            return false;
        }

        @Override
        public long delete(Specification<TestObject> spec) {
            return 0;
        }

        @Override
        public <S extends TestObject, R> R findBy(
                Specification<TestObject> spec,
                Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public <S extends TestObject> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public List<TestObject> findAll() {
            return null;
        }

        @Override
        public List<TestObject> findAllById(Iterable<Integer> integers) {
            return null;
        }

        @Override
        public <S extends TestObject> S save(S entity) {
            return null;
        }

        @Override
        public Optional<TestObject> findById(Integer integer) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Integer integer) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Integer integer) {
        }

        @Override
        public void delete(TestObject entity) {
        }

        @Override
        public void deleteAllById(Iterable<? extends Integer> integers) {
        }

        @Override
        public void deleteAll(Iterable<? extends TestObject> entities) {
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public List<TestObject> findAll(Sort sort) {
            return null;
        }

        @Override
        public Page<TestObject> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends TestObject> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends TestObject> Page<S> findAll(Example<S> example,
                Pageable pageable) {
            return null;
        }

        @Override
        public <S extends TestObject> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends TestObject> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends TestObject, R> R findBy(Example<S> example,
                Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    }

    @BrowserCallable
    static class DefaultJpaRepositoryService
            extends CrudRepositoryService<TestObject, Integer, TestRepository> {
    }

    @BrowserCallable
    static class CustomCrudRepositoryService extends
            CrudRepositoryService<TestObject, Integer, CustomCrudRepository> {
        public CustomCrudRepositoryService(CustomCrudRepository repository) {
            super(repository);
        }
    }

    @BrowserCallable
    static class CustomJpaRepositoryService extends
            CrudRepositoryService<TestObject, Integer, CustomJpaRepository> {
        public CustomJpaRepositoryService(CustomJpaRepository repository) {
            super(repository);
        }
    }

    @Autowired
    private DefaultJpaRepositoryService defaultJpaRepositoryService;

    @Autowired
    private CustomCrudRepositoryService customCrudRepositoryService;

    @Autowired
    private CustomJpaRepositoryService customJpaRepositoryService;

    @Test
    public void when_serviceHasNoExplicitConstructor_then_getRepositoryResolvesRepositoryFromContext() {
        assertNotNull(defaultJpaRepositoryService.getRepository());
    }

    @Test
    public void when_serviceHasExplicitConstructor_then_getRepositoryReturnsTheProvidedRepositoryInstance() {
        assertNotNull(customCrudRepositoryService.getRepository());
        assertEquals(CustomCrudRepository.class, customCrudRepositoryService
                .getRepository().getClass().getSuperclass());
    }

    @Test
    public void JpaRepository_Is_CompatibleWith_CrudRepositoryService() {
        assertNotNull(defaultJpaRepositoryService.getRepository());
    }

    @Test
    public void customJpaRepository_Is_CompatibleWith_CrudRepositoryService() {
        assertNotNull(customJpaRepositoryService.getRepository());
        assertEquals(CustomJpaRepository.class, customJpaRepositoryService
                .getRepository().getClass().getSuperclass());
    }

}
