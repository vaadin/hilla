/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.crud;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vaadin.hilla.crud.filter.AndFilter;
import com.vaadin.hilla.crud.filter.Filter;
import com.vaadin.hilla.crud.filter.OrFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter;
import com.vaadin.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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

    @Test
    public void filterStringPropertyUsingContains() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = new PropertyStringFilter("name",
                Matcher.CONTAINS, "Jo");
        assertFilteredNames(filter, "John", "Johnny", "Josh");
    }

    @Test
    public void filterStringPropertyUsingEquals() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = new PropertyStringFilter("name",
                Matcher.EQUALS, "John");
        assertFilteredNames(filter, "John");
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingLessThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = new PropertyStringFilter("name",
                Matcher.LESS_THAN, "John");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterStringPropertyUsingGreaterThan() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = new PropertyStringFilter("name",
                Matcher.GREATER_THAN, "John");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterUUIDPropertyUsingContains() {
        setupUUIDs();
        String filterValue = UUID.randomUUID().toString();
        PropertyStringFilter filter = new PropertyStringFilter("uuidValue",
                Matcher.CONTAINS, filterValue);
        executeFilter(filter);
    }

    @Test
    public void filterUUIDPropertyUsingEquals() {
        List<TestObject> created = setupUUIDs();
        TestObject to = created.get(0);
        String filterValue = to.getUuidValue().toString();
        PropertyStringFilter filter = new PropertyStringFilter("uuidValue",
                Matcher.EQUALS, filterValue);
        assertFilterResult(filter, List.of(to));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterUUIDPropertyUsingLessThan() {
        setupUUIDs();
        String filterValue = UUID.randomUUID().toString();
        PropertyStringFilter filter = new PropertyStringFilter("uuidValue",
                Matcher.LESS_THAN, filterValue);
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterUUIDPropertyUsingGreaterThan() {
        setupUUIDs();
        String filterValue = UUID.randomUUID().toString();
        PropertyStringFilter filter = new PropertyStringFilter("uuidValue",
                Matcher.GREATER_THAN, filterValue);
        executeFilter(filter);
    }

    @Test
    public void filterNumberPropertyUsingContains() {
        setupNumbers();

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter("intValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter(
                    "nullableIntValue", Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter("longValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter(
                    "nullableLongValue", Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter("floatValue",
                    Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter(
                    "nullableFloatValue", Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter(
                    "doubleValue", Matcher.CONTAINS, "2");
            executeFilter(filter);
        });

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            PropertyStringFilter filter = new PropertyStringFilter(
                    "nullableDoubleValue", Matcher.CONTAINS, "2");
            executeFilter(filter);
        });
    }

    @Test
    public void filterNumberPropertyUsingEquals() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = new PropertyStringFilter("intValue",
                Matcher.EQUALS, "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("nullableIntValue", Matcher.EQUALS,
                "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("longValue", Matcher.EQUALS, "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("nullableLongValue", Matcher.EQUALS,
                "4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("floatValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("nullableFloatValue", Matcher.EQUALS,
                "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("doubleValue", Matcher.EQUALS, "0.4");
        assertFilterResult(filter, List.of(created.get(4)));

        filter = new PropertyStringFilter("nullableDoubleValue", Matcher.EQUALS,
                "0.4");
        assertFilterResult(filter, List.of(created.get(4)));
    }

    @Test
    public void filterNumberPropertyUsingLessThan() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = new PropertyStringFilter("intValue",
                Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("nullableIntValue", Matcher.LESS_THAN,
                "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("longValue", Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("nullableLongValue",
                Matcher.LESS_THAN, "4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("floatValue", Matcher.LESS_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("nullableFloatValue",
                Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("doubleValue", Matcher.LESS_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(0, 4));

        filter = new PropertyStringFilter("nullableDoubleValue",
                Matcher.LESS_THAN, "0.4");
        assertFilterResult(filter, created.subList(0, 4));
    }

    @Test
    public void filterNumberPropertyUsingGreaterThan() {
        List<TestObject> created = setupNumbers();

        PropertyStringFilter filter = new PropertyStringFilter("intValue",
                Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("nullableIntValue",
                Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("longValue", Matcher.GREATER_THAN,
                "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("nullableLongValue",
                Matcher.GREATER_THAN, "4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("floatValue", Matcher.GREATER_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("nullableFloatValue",
                Matcher.GREATER_THAN, "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("doubleValue", Matcher.GREATER_THAN,
                "0.4");
        assertFilterResult(filter, created.subList(5, 10));

        filter = new PropertyStringFilter("nullableDoubleValue",
                Matcher.GREATER_THAN, "0.4");
        assertFilterResult(filter, created.subList(5, 10));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingContains() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("booleanValue",
                Matcher.CONTAINS, "True");
        executeFilter(filter);
    }

    @Test
    public void filterBooleanPropertyUsingEquals() {
        setupBooleans();

        PropertyStringFilter filter = new PropertyStringFilter("booleanValue",
                Matcher.EQUALS, "True");
        List<TestObject> testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertTrue(testObjects.get(0).getBooleanValue());

        filter = new PropertyStringFilter("booleanValue", Matcher.EQUALS,
                "False");
        testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertFalse(testObjects.get(0).getBooleanValue());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingLessThan() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("booleanValue",
                Matcher.LESS_THAN, "True");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterBooleanPropertyUsingGreaterThan() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("booleanValue",
                Matcher.GREATER_THAN, "True");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingContains() {
        setupEnums();
        PropertyStringFilter filter = new PropertyStringFilter("enumValue",
                Matcher.CONTAINS, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test
    public void filterEnumPropertyUsingEquals() {
        setupEnums();

        PropertyStringFilter filter = new PropertyStringFilter("enumValue",
                Matcher.EQUALS, TestEnum.TEST1.name());
        List<TestObject> testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertEquals(TestEnum.TEST1, testObjects.get(0).getEnumValue());

        filter = new PropertyStringFilter("enumValue", Matcher.EQUALS,
                TestEnum.TEST2.name());
        testObjects = executeFilter(filter);

        assertEquals(1, testObjects.size());
        Assert.assertEquals(TestEnum.TEST2, testObjects.get(0).getEnumValue());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingLessThan() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("enumValue",
                Matcher.LESS_THAN, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterEnumPropertyUsingGreaterThan() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("enumValue",
                Matcher.GREATER_THAN, TestEnum.TEST1.name());
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterUnknownEnumValue() {
        setupBooleans();
        PropertyStringFilter filter = new PropertyStringFilter("enumValue",
                Matcher.EQUALS, "FOO");
        executeFilter(filter);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void filterNonExistingProperty() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter = new PropertyStringFilter("foo",
                Matcher.EQUALS, "John");
        executeFilter(filter);
    }

    @Test
    public void basicOrFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = new PropertyStringFilter("name",
                Matcher.EQUALS, "John");
        PropertyStringFilter filter2 = new PropertyStringFilter("name",
                Matcher.EQUALS, "Polly");
        OrFilter filter = new OrFilter(filter1, filter2);
        assertFilteredNames(filter, "John", "Polly");
    }

    @Test
    public void basicAndFilter() {
        setupNames("Jack", "John", "Johnny", "Polly", "Josh");
        PropertyStringFilter filter1 = new PropertyStringFilter("name",
                Matcher.CONTAINS, "Joh");
        PropertyStringFilter filter2 = new PropertyStringFilter("name",
                Matcher.CONTAINS, "nny");
        AndFilter filter = new AndFilter(filter1, filter2);
        assertFilteredNames(filter, "Johnny");
    }

    @Test
    public void nestedPropertyFilterString() {
        setupNestedObjects();
        PropertyStringFilter filter = new PropertyStringFilter(
                "nestedObject.name", Matcher.CONTAINS, "42");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 1", testObject.getName());
        assertEquals(42, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void nestedPropertyFilterNumber() {
        setupNestedObjects();
        PropertyStringFilter filter = new PropertyStringFilter(
                "nestedObject.luckyNumber", Matcher.EQUALS, "84");
        List<TestObject> result = executeFilter(filter);
        assertEquals(1, result.size());
        TestObject testObject = result.get(0);
        assertEquals("some name 2", testObject.getName());
        assertEquals(84, testObject.getNestedObject().getLuckyNumber());
    }

    @Test
    public void nestedPropertyFilterNumberNoResult() {
        setupNestedObjects();
        PropertyStringFilter filter = new PropertyStringFilter(
                "nestedObject.luckyNumber", Matcher.EQUALS, "85");
        assertEquals(0, executeFilter(filter).size());
    }

    @Test
    public void secondLevelNestedPropertyFilterString() {
        setupNestedObjects();
        PropertyStringFilter filter = new PropertyStringFilter(
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
        PropertyStringFilter filter = new PropertyStringFilter(
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
        PropertyStringFilter filter = new PropertyStringFilter(
                "nestedObject.secondLevelNestedObject.luckyNumber",
                Matcher.EQUALS, "3");
        assertEquals(0, executeFilter(filter).size());
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
        Specification<TestObject> spec = JpaFilterConverter.toSpec(filter,
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

    private List<TestObject> setupUUIDs() {
        List<TestObject> created = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestObject testObject = new TestObject();
            testObject.setUuidValue(UUID.randomUUID());
            entityManager.persist(testObject);
            created.add(testObject);
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
