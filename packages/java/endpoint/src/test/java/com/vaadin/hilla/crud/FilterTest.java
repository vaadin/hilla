package com.vaadin.hilla.crud;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.hilla.crud.filter.AndFilter;
import com.vaadin.hilla.crud.filter.Filter;
import com.vaadin.hilla.crud.filter.OrFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
        PropertyStringFilter filter = createFilter("name", Matcher.CONTAINS,
                "Jo");
        assertFilteredNames(filter, "John", "Johnny", "Josh");
    }

    @Test
    public void filterStringPropertyUsingEquals() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createFilter("name", Matcher.EQUALS,
                "John");
        assertFilteredNames(filter, "John");
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingLessThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createFilter("name", Matcher.LESS_THAN,
                "John");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingGreaterThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createFilter("name", Matcher.GREATER_THAN,
                "John");
        executeFilter(filter);
    }

    @Test
    public void filterNumberPropertyUsingContains() {
        setupNumbers();

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("intValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("nullableIntValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("longValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("nullableLongValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("floatValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("nullableFloatValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("doubleValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = createFilter("nullableDoubleValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });
    }

    @Test
    public void filterNumberPropertyUsingEquals() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = createFilter("intValue", Matcher.EQUALS,
                "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("nullableIntValue", Matcher.EQUALS, "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("longValue", Matcher.EQUALS, "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("nullableLongValue", Matcher.EQUALS, "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("floatValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("nullableFloatValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("doubleValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = createFilter("nullableDoubleValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));
    }

    @Test
    public void filterNumberPropertyUsingLessThan() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = createFilter("intValue",
                Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("nullableIntValue", Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("longValue", Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("nullableLongValue", Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("floatValue", Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("nullableFloatValue", Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("doubleValue", Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = createFilter("nullableDoubleValue", Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));
    }

    @Test
    public void filterNumberPropertyUsingGreaterThan() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = createFilter("intValue",
                Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("nullableIntValue", Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("longValue", Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("nullableLongValue", Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("floatValue", Matcher.GREATER_THAN, "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("nullableFloatValue", Matcher.GREATER_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("doubleValue", Matcher.GREATER_THAN, "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = createFilter("nullableDoubleValue", Matcher.GREATER_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(5, 10));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingContains() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("booleanValue",
                Matcher.CONTAINS, "True");
        executeFilter(filter);
    }

    @Test
    public void filterBooleanPropertyUsingEquals() {
        setupBooleans();

        PropertyStringFilter filter = createFilter("booleanValue",
                Matcher.EQUALS, "True");
        List<TestObject> testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertTrue(testObjects.get(0).getBooleanValue());

        filter = createFilter("booleanValue", Matcher.EQUALS, "False");
        testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertFalse(testObjects.get(0).getBooleanValue());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingLessThan() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("booleanValue",
                Matcher.LESS_THAN, "True");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingGreaterThan() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("booleanValue",
                Matcher.GREATER_THAN, "True");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingContains() {
        setupEnums();
        PropertyStringFilter filter = createFilter("enumValue",
                Matcher.CONTAINS, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test
    public void filterEnumPropertyUsingEquals() {
        setupEnums();

        PropertyStringFilter filter = createFilter("enumValue", Matcher.EQUALS,
                TestEnum.TEST1.name());
        List<TestObject> testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertEquals(TestEnum.TEST1, testObjects.get(0).getEnumValue());

        filter = createFilter("enumValue", Matcher.EQUALS,
                TestEnum.TEST2.name());
        testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertEquals(TestEnum.TEST2, testObjects.get(0).getEnumValue());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingLessThan() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("enumValue",
                Matcher.LESS_THAN, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingGreaterThan() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("enumValue",
                Matcher.GREATER_THAN, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterUnknownEnumValue() {
        setupBooleans();
        PropertyStringFilter filter = createFilter("enumValue", Matcher.EQUALS,
                "FOO");
        executeFilter(filter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void filterNonExistingProperty() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = createFilter("foo", Matcher.EQUALS,
                "John");
        executeFilter(filter);
    }

    @Test
    public void basicOrFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = createFilter("name", Matcher.EQUALS,
                "John");
        PropertyStringFilter filter2 = createFilter("name", Matcher.EQUALS,
                "Polly");
        OrFilter filter = new OrFilter();
        filter.setChildren(List.of(filter1, filter2));
        assertFilteredNames(filter, "John", "Polly");
    }

    @Test
    public void basicAndFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = createFilter("name", Matcher.CONTAINS,
                "Joh");
        PropertyStringFilter filter2 = createFilter("name", Matcher.CONTAINS,
                "nny");
        AndFilter filter = new AndFilter();
        filter.setChildren(List.of(filter1, filter2));
        assertFilteredNames(filter, "Johnny");
    }

    @Test
    public void nestedPropertyFilterString() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter("nestedObject.name",
                Matcher.CONTAINS, "42");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 1", testObject.getName());
        assertEquals(42, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void nestedPropertyFilterNumber() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter("nestedObject.luckyNumber",
                Matcher.EQUALS, "84");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 2", testObject.getName());
        assertEquals(84, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void nestedPropertyFilterNumberNoResult() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter("nestedObject.luckyNumber",
                Matcher.EQUALS, "85");
        assertEquals(0, executeFilter(filter).size());
    }

    @Test
    public void secondLevelNestedPropertyFilterString() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter(
                "nestedObject.secondLevelNestedObject.name", Matcher.CONTAINS,
                "second level nested object 1");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 1", testObject.getName());
        assertEquals(42, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void secondLevelNestedPropertyFilterNumber() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter(
                "nestedObject.secondLevelNestedObject.luckyNumber",
                Matcher.EQUALS, "2");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 2", testObject.getName());
        assertEquals(84, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void secondLevelNestedPropertyFilterNumberNoResult() {
        setupNestedObjects();
        PropertyStringFilter filter = createFilter(
                "nestedObject.secondLevelNestedObject.luckyNumber",
                Matcher.EQUALS, "3");
        assertEquals(0, executeFilter(filter).size());
    }

    private PropertyStringFilter createFilter(String propertyPath,
            Matcher matcher, String filterValue) {
        PropertyStringFilter filter = new PropertyStringFilter();
        filter.setPropertyId(propertyPath);
        filter.setFilterValue(filterValue);
        filter.setMatcher(matcher);
        return filter;
    }

    private void assertFilteredNames(Filter filter, String... expectedNames) {
        List<TestObject> result = executeFilter(filter);
        assertEquals(expectedNames.length, result.size());
        Object[] actual = result.stream().map(TestObject::getName).toArray();
        Assert.assertArrayEquals(expectedNames, actual);
    }

    private void assertFilterResult(Filter filter, List<TestObject> result) {
        List<TestObject> actual = executeFilter(filter);
        assertEquals(result, actual);
    }

    private List<TestObject> executeFilter(Filter filter) {
        Specification<TestObject> spec = jpaFilterConverter.toSpec(filter,
                TestObject.class);
        return repository.findAll(spec);
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

    private List<TestObject> setupNumbers() {
        List<TestObject> created = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestObject testObject = new TestObject();
            testObject.setIntValue(i);
            testObject.setNullableIntValue(i);
            testObject.setLongValue(i);
            testObject.setNullableLongValue((long) i);
            testObject.setFloatValue((float) i / 10);
            testObject.setNullableFloatValue((float) i / 10);
            testObject.setDoubleValue((double) i / 10);
            testObject.setNullableDoubleValue((double) i / 10);
            entityManager.persist(testObject);
            created.add(testObject);
        }
        entityManager.flush();
        return created;
    }

    private void setupEnums() {
        TestObject testObject = new TestObject();
        testObject.setEnumValue(TestEnum.TEST1);
        entityManager.persist(testObject);
        testObject = new TestObject();
        testObject.setEnumValue(TestEnum.TEST2);
        entityManager.persist(testObject);
        entityManager.flush();
    }

    private void setupNestedObjects() {
        SecondLevelNestedObject secondLevelNestedObject1 = new SecondLevelNestedObject();
        secondLevelNestedObject1.setName("second level nested object 1");
        secondLevelNestedObject1.setLuckyNumber(1);
        entityManager.persist(secondLevelNestedObject1);
        SecondLevelNestedObject secondLevelNestedObject2 = new SecondLevelNestedObject();
        secondLevelNestedObject2.setName("second level nested object 2");
        secondLevelNestedObject2.setLuckyNumber(2);
        entityManager.persist(secondLevelNestedObject2);
        NestedObject nestedObject1 = new NestedObject();
        nestedObject1.setName("nested object 42");
        nestedObject1.setLuckyNumber(42);
        nestedObject1.setSecondLevelNestedObject(secondLevelNestedObject1);
        entityManager.persist(nestedObject1);
        NestedObject nestedObject2 = new NestedObject();
        nestedObject2.setName("nested object 84");
        nestedObject2.setLuckyNumber(84);
        nestedObject2.setSecondLevelNestedObject(secondLevelNestedObject2);
        entityManager.persist(nestedObject2);
        TestObject testObject1 = new TestObject();
        testObject1.setName("some name 1");
        testObject1.setNestedObject(nestedObject1);
        entityManager.persist(testObject1);
        TestObject testObject2 = new TestObject();
        testObject2.setName("some name 2");
        testObject2.setNestedObject(nestedObject2);
        entityManager.persist(testObject2);
        entityManager.flush();
    }

}
