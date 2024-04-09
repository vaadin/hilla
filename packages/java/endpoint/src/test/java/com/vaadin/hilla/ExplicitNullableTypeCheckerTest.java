/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.hilla;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.vaadin.hilla.endpoints.Id;

public class ExplicitNullableTypeCheckerTest {
    private ExplicitNullableTypeChecker explicitNullableTypeChecker;
    private ExplicitNullableTypeCheckerHelper helper;
    private Type stringListType;
    private Type stringToDateMapType;
    private Type stringArrayType;

    @Before
    public void setup() throws NoSuchMethodException {
        explicitNullableTypeChecker = new ExplicitNullableTypeChecker();
        helper = new ExplicitNullableTypeCheckerHelper(false);

        stringListType = getClass()
                .getMethod("parametrizedListMethod", String[].class)
                .getGenericReturnType();

        stringToDateMapType = getClass()
                .getMethod("parametrizedMapMethod", Date[].class)
                .getGenericReturnType();

        stringArrayType = getClass().getMethod("arrayMethod", String[].class)
                .getGenericReturnType();
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForPrimitiveType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0,
                byte.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0,
                short.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0,
                int.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0L,
                long.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0.0f,
                float.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0.0d,
                double.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(true,
                boolean.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType('a',
                char.class, false));
    }

    @Test
    public void should_ReturnNull_When_GivenNullValue_ForVoidType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(null,
                void.class, false));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(null,
                Void.class, false));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForStringType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType("",
                String.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForStringType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                String.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("String"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForDateType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new Date(), Date.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForDateType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Date.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Date"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForDateTimeType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                LocalDateTime.now(), LocalDateTime.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForDateTimeType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                LocalDateTime.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("LocalDateTime"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForLocalTimeType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(LocalTime.now(), LocalTime.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForLcalTimeType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                LocalTime.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("LocalTime"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForMapType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                new HashMap<String, String>(), Map.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForMapType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Map.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Map"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForObjectType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new Object(), Object.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForObjectType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Object.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Object"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForOptionalType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(Optional.empty(), Optional.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForOptionalType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Optional.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Optional.empty"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForCollectionType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                new ArrayList<String>(), stringListType, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForCollectionType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                stringListType, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("List"));
        Assert.assertTrue(error.contains("String"));
    }

    @Test
    public void should_Recursively_Check_List_Items() {
        ExplicitNullableTypeCheckerHelper checker = spy(helper);

        List<String> list = parametrizedListMethod("foo", "bar");

        checker.checkValueForType(list, stringListType);
        // The first interaction is the obvious
        verify(checker).checkValueForType(list, stringListType);

        verify(checker).checkValueForType("foo", String.class);
        verify(checker).checkValueForType("bar", String.class);
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullItems_InListType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                Arrays.asList("", ""), stringListType, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullItem_InListType() {
        String error = explicitNullableTypeChecker.checkValueForType(
                Arrays.asList("", null, ""), stringListType, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("List"));
        Assert.assertTrue(error.contains("String"));
    }

    @Test
    public void should_Recursively_Check_Map_Values() {
        ExplicitNullableTypeCheckerHelper checker = spy(helper);

        Date dateOne = new Date();
        Date dateTwo = new Date();
        Map<String, Date> map = parametrizedMapMethod(dateOne, dateTwo);

        checker.checkValueForType(map, stringToDateMapType);
        // The first interaction is the obvious
        verify(checker).checkValueForType(map, stringToDateMapType);

        verify(checker).checkValueForType(dateOne, Date.class);
        verify(checker).checkValueForType(dateTwo, Date.class);
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValues_InMapType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                parametrizedMapMethod(new Date(), new Date()),
                stringToDateMapType, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullValues_InMapType() {
        String error = explicitNullableTypeChecker.checkValueForType(
                parametrizedMapMethod(new Date(), null), stringToDateMapType,
                false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null value"));
        Assert.assertTrue(error.contains("key 'null'"));
        Assert.assertTrue(error.contains("Map"));
        Assert.assertTrue(error.contains("Date"));
    }

    @Test
    public void should_Recursively_Check_GenericArray_Items() {
        ExplicitNullableTypeCheckerHelper checker = spy(helper);

        String[] array = arrayMethod("foo", "bar");

        checker.checkValueForType(array, stringArrayType);
        // The first interaction is the obvious
        verify(checker).checkValueForType(array, stringArrayType);

        verify(checker).checkValueForType("foo", String.class);
        verify(checker).checkValueForType("bar", String.class);
    }

    @Test
    public void should_ReturnNull_When_GivenNonNull_BeanProperties() {
        final Bean bean = new Bean();
        bean.setTitle("foo");

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(bean,
                Bean.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNull_BeanProperty() {
        // Property 'title' is null
        Bean bean = new Bean();

        String error = explicitNullableTypeChecker.checkValueForType(bean,
                Bean.class, false);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Bean"));
    }

    @Test
    public void should_Recursively_Check_BeanProperties() {
        ExplicitNullableTypeCheckerHelper checker = spy(helper);

        final Bean bean = new Bean();
        bean.setTitle("foo");
        bean.description = "bar";

        checker.checkValueForType(bean, Bean.class);
        // The first interaction is the obvious
        verify(checker).checkValueForType(bean, Bean.class);

        verify(checker).checkValueForType("foo", String.class);
        // Should not check non-bean properties
        verify(checker, never()).checkValueForType("bar", String.class);
    }

    @Test
    public void should_ReturnNull_When_AnnotatedNullable()
            throws NoSuchMethodException {
        String error = explicitNullableTypeChecker
                .checkValueForAnnotatedElement(null,
                        getClass().getMethod("stringNullable"), false);

        Assert.assertNull("Nullable return type should allow null value",
                error);

        error = explicitNullableTypeChecker.checkValueForAnnotatedElement(
                "Not null value", getClass().getMethod("stringNullable"),
                false);

        Assert.assertNull("Nullable return type should allow null value",
                error);
    }

    @Test
    public void should_ReturnNull_When_AnnotatedId()
            throws NoSuchMethodException {
        String error = explicitNullableTypeChecker
                .checkValueForAnnotatedElement(null,
                        getClass().getMethod("methodWithIdAnnotation"), false);

        Assert.assertNull("Method with @Id annotation should allow null value",
                error);

        error = explicitNullableTypeChecker.checkValueForAnnotatedElement(1l,
                getClass().getMethod("methodWithIdAnnotation"), false);

        Assert.assertNull(
                "Method with @Id annotation should allow non-null value",
                error);
    }

    @Test
    public void should_InvokeCheckValueForType_When_AnnotatedNonnull()
            throws NoSuchMethodException {
        explicitNullableTypeChecker = spy(explicitNullableTypeChecker);
        String notNullValue = "someValue";
        String error = explicitNullableTypeChecker
                .checkValueForAnnotatedElement(notNullValue,
                        getClass().getMethod("stringNonnull"), false);

        Assert.assertNull("Should allow values with @Nonnull", error);

        verify(explicitNullableTypeChecker).checkValueForType(notNullValue,
                String.class, false);
    }

    @Test
    public void should_ReturnNull_When_GivenNonNull_Generic_BeanProperties() {
        final Person person = new Person();
        person.setName("foo");
        person.setId(1);

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(person,
                Person.class, false));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNull_CircularReference_BeanProperties() {
        final Employee employee = new Employee();
        final Company company = new Company();

        employee.setId(1);
        employee.setCompany(company);

        company.setId(1);
        company.setEmployees(Arrays.asList(employee));

        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(employee, Employee.class, false));

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(company,
                Company.class, false));
    }

    @Test
    public void should_ReturnNull_When_GivenNullAndNotRequiredByContext() {
        Assert.assertNotNull(explicitNullableTypeChecker.checkValueForType(null,
                Employee.class, false));
    }

    @Test
    public void should_ReturnError_When_GivenNullAndRequiredByContext() {
        Assert.assertNotNull(explicitNullableTypeChecker.checkValueForType(null,
                Employee.class, true));
    }

    @Test
    public void should_ReturnError_When_GivenNestedNullAndRequiredByContext() {
        Employee employee = new Employee();
        employee.setId(12);
        employee.setCompany(null);
        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForType(employee, Employee.class, true));
    }

    public List<String> parametrizedListMethod(String... args) {
        final List<String> list = new ArrayList<String>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }

    public Map<String, Date> parametrizedMapMethod(Date... args) {
        final Map<String, Date> map = new HashMap<String, Date>();
        for (Date arg : args) {
            map.put(String.valueOf(arg), arg);
        }
        return map;
    }

    public String[] arrayMethod(String... args) {
        return args;
    }

    /**
     * Method for testing
     */
    @Nullable
    public String stringNullable() {
        return "";
    }

    /**
     * Method for testing
     */
    @Id
    public Long methodWithIdAnnotation() {
        return 1L;
    }

    /**
     * Method for testing
     */
    @Nonnull
    public String stringNonnull() {
        return "";
    }

    static private class Bean {
        static String staticProperty;
        @JsonIgnore
        public String ignoreProperty;
        public String description;
        transient String transientProperty;
        @Nonnull
        private String title;

        public Bean() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    static private abstract class AbstractEntity<ID> {
        private ID id;

        public ID getId() {
            return id;
        }

        public void setId(ID id) {
            this.id = id;
        }
    }

    static private class Person extends AbstractEntity<Integer> {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static private class Employee extends AbstractEntity<Integer> {
        private Company company;

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }
    }

    static private class Company extends AbstractEntity<Integer> {
        private List<Employee> employees;

        public List<Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employee> employees) {
            this.employees = employees;
        }
    }
}
