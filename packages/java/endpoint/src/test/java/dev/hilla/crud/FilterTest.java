package dev.hilla.crud;

import java.util.ArrayList;
import java.util.List;

import dev.hilla.crud.filter.AndFilter;
import dev.hilla.crud.filter.Filter;
import dev.hilla.crud.filter.OrFilter;
import dev.hilla.crud.filter.PropertyStringFilter;
import dev.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest()
public class FilterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TestRepository repository;
    @Autowired
    private JpaFilterConverter jpaFilterConverter;

    @Test
    public void filterStringPropertyUsingContains() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createNameFilter(Matcher.CONTAINS, "Jo");
        assertFilterResult(filter, "John", "Johnny", "Josh");
    }

    @Test
    public void filterStringPropertyUsingEquals() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createNameFilter(Matcher.EQUALS, "John");
        assertFilterResult(filter, "John");
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingLessThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createNameFilter(Matcher.LESS_THAN,
                "John");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingGreaterThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createNameFilter(Matcher.GREATER_THAN,
                "John");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterNumberPropertyUsingContains() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createIdFilter(Matcher.CONTAINS, "2");
        executeFilter(filter);
    }

    @Test
    public void filterNumberPropertyUsingEquals() {
        List<TestObject> created = setupNames("Jack", "John", "Johnny", "Polly",
                "Josh");
        Integer johnId = created.get(1).getId();
        PropertyStringFilter filter = createIdFilter(Matcher.EQUALS,
                johnId + "");
        assertFilterResult(filter, "John");
    }

    @Test
    public void filterNumberPropertyUsingLessThan() {
        List<TestObject> created = setupNames("Jack", "John", "Johnny", "Polly",
                "Josh");
        Integer johnnyId = created.get(2).getId();
        PropertyStringFilter filter = createIdFilter(Matcher.LESS_THAN,
                johnnyId + "");
        assertFilterResult(filter, "Jack", "John");
    }

    @Test
    public void filterNumberPropertyUsingGreaterThan() {
        List<TestObject> created = setupNames("Jack", "John", "Johnny", "Polly",
                "Josh");
        Integer johnnyId = created.get(2).getId();
        PropertyStringFilter filter = createIdFilter(Matcher.GREATER_THAN,
                johnnyId + "");
        assertFilterResult(filter, "Polly", "Josh");
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingContains() {
        setupBooleans();
        PropertyStringFilter filter = createBooleanFilter(Matcher.CONTAINS,
                "True");
        executeFilter(filter);
    }

    @Test
    public void filterBooleanPropertyUsingEquals() {
        setupBooleans();

        PropertyStringFilter filter = createBooleanFilter(Matcher.EQUALS,
                "True");
        List<TestObject> testObjects = executeFilter(filter);

        Assert.assertEquals(1, testObjects.size());
        Assert.assertTrue(testObjects.get(0).getBooleanValue());

        filter = createBooleanFilter(Matcher.EQUALS, "False");
        testObjects = executeFilter(filter);

        Assert.assertEquals(1, testObjects.size());
        Assert.assertFalse(testObjects.get(0).getBooleanValue());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingLessThan() {
        setupBooleans();
        PropertyStringFilter filter = createBooleanFilter(Matcher.LESS_THAN,
                "True");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingGreaterThan() {
        setupBooleans();
        PropertyStringFilter filter = createBooleanFilter(Matcher.GREATER_THAN,
                "True");
        executeFilter(filter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void filterNonExistingProperty() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createNameFilter(Matcher.EQUALS, "John");
        filter.setPropertyId("foo");
        assertFilterResult(filter, "John");
    }

    @Test
    public void basicOrFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = createNameFilter(Matcher.EQUALS, "John");
        PropertyStringFilter filter2 = createNameFilter(Matcher.EQUALS,
                "Polly");
        OrFilter filter = new OrFilter();
        filter.setChildren(List.of(filter1, filter2));
        assertFilterResult(filter, "John", "Polly");
    }

    @Test
    public void basicAndFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = createNameFilter(Matcher.CONTAINS,
                "Joh");
        PropertyStringFilter filter2 = createNameFilter(Matcher.CONTAINS,
                "nny");
        AndFilter filter = new AndFilter();
        filter.setChildren(List.of(filter1, filter2));
        assertFilterResult(filter, "Johnny");
    }

    private void assertFilterResult(Filter filter, String... expectedNames) {
        List<TestObject> result = executeFilter(filter);
        assertFilterResult(result, expectedNames);
    }

    private void assertFilterResult(List<TestObject> result, String... names) {
        Assert.assertEquals(names.length, result.size());
        Object[] actual = result.stream().map(o -> o.getName()).toArray();
        Assert.assertArrayEquals(names, actual);
    }

    private List<TestObject> executeFilter(Filter filter) {
        Specification<TestObject> spec = jpaFilterConverter.toSpec(filter,
                TestObject.class);
        return repository.findAll(spec);
    }

    private PropertyStringFilter createNameFilter(Matcher matcher,
            String filterString) {
        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId("name");
        filter.setFilterValue(filterString);
        filter.setMatcher(matcher);
        return filter;
    }

    private PropertyStringFilter createIdFilter(Matcher matcher,
            String filterString) {
        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId("id");
        filter.setFilterValue(filterString);
        filter.setMatcher(matcher);
        return filter;
    }

    private PropertyStringFilter createBooleanFilter(Matcher matcher,
            String filterString) {
        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId("booleanValue");
        filter.setFilterValue(filterString);
        filter.setMatcher(matcher);
        return filter;
    }

    private List<TestObject> setupNames(String... names) {
        List<TestObject> created = new ArrayList<>();
        for (String name : names) {
            TestObject testObject = new TestObject();
            testObject.setName(name);
            created.add(entityManager.persist(testObject));
        }
        entityManager.flush();
        return created;
    }

    private void setupBooleans() {
        TestObject testObject = new TestObject();
        testObject.setBooleanValue(true);
        entityManager.persist(testObject);
        testObject = new TestObject();
        testObject.setBooleanValue(false);
        entityManager.persist(testObject);
        entityManager.flush();
    }
}
