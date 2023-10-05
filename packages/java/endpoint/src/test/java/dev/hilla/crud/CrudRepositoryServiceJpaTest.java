package dev.hilla.crud;

import java.util.ArrayList;
import java.util.List;

import dev.hilla.crud.filter.PropertyStringFilter;
import dev.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest()
@Import(TestCrudRepositoryService.class)
public class CrudRepositoryServiceJpaTest {

    @Autowired
    TestRepository jpaRepository;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    TestCrudRepositoryService testCrudRepositoryService;

    private List<TestObject> testObjects;

    @Before
    public void setupDB() {
        String[] names = new String[] { "John", "Jeff", "Michael", "Michelle",
                "Dana", "Lady" };
        testObjects = new ArrayList<>();
        for (String name : names) {
            TestObject testObject = new TestObject();
            testObject.setName(name);
            testObjects.add(entityManager.persist(testObject));
        }
        entityManager.flush();
    }

    @Test
    public void count() {
        Assert.assertEquals(6, testCrudRepositoryService.count(null));

        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId("name");
        filter.setMatcher(Matcher.CONTAINS);
        filter.setFilterValue("Mich");
        Assert.assertEquals(2, testCrudRepositoryService.count(filter));
    }

    @Test
    public void get() {
        TestObject object = testObjects.get(2);
        Assert.assertEquals(object.getName(),
                testCrudRepositoryService.get(object.getId()).getName());
        Assert.assertNull(testCrudRepositoryService.get(object.getId() + 10));
    }

    @Test
    public void exists() {
        TestObject object = testObjects.get(3);
        Assert.assertTrue(testCrudRepositoryService.exists(object.getId()));
        Assert.assertFalse(
                testCrudRepositoryService.exists(object.getId() + 10));
    }

    @Test
    public void saveAll() {
        TestObject o1 = new TestObject();
        o1.setName("Hello");
        TestObject o2 = new TestObject();
        o2.setName("World");

        List<TestObject> saved = testCrudRepositoryService
                .saveAll(List.of(o1, o2));
        Assert.assertEquals("World", saved.get(1).getName());
        Assert.assertTrue(
                testCrudRepositoryService.exists(saved.get(1).getId()));
    }

    @Test
    public void deleteAll() {
        testCrudRepositoryService.deleteAll(List.of(testObjects.get(3).getId(),
                testObjects.get(4).getId()));
        Assert.assertEquals(List.of("John", "Jeff", "Michael", "Lady"),
                testCrudRepositoryService.list(Pageable.unpaged(), null)
                        .stream().map(o -> o.getName()).toList());
    }
}
