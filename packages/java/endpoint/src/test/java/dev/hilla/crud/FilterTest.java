package dev.hilla.crud;

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

    @Test(expected = IllegalArgumentException.class)
    public void filterNonExistingStringProperty() {
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
        Specification<TestObject> spec = jpaFilterConverter.toSpec(filter,
                TestObject.class);
        List<TestObject> result = repository.findAll(spec);
        assertFilterResult(result, expectedNames);
    }

    private void assertFilterResult(List<TestObject> result, String... names) {
        Assert.assertEquals(names.length, result.size());
        Object[] actual = result.stream().map(o -> o.getName()).toArray();
        Assert.assertArrayEquals(names, actual);
    }

    private PropertyStringFilter createNameFilter(Matcher matcher,
            String filterString) {
        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId("name");
        filter.setFilterValue(filterString);
        filter.setMatcher(matcher);
        return filter;
    }

    private void setupNames(String... names) {
        for (String name : names) {
            TestObject testObject = new TestObject();
            testObject.setName(name);
            entityManager.persist(testObject);
        }
        entityManager.flush();

    }
}
