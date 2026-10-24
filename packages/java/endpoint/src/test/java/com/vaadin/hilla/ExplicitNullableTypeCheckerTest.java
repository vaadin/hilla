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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.Nullable;

import com.vaadin.hilla.endpoints.Id;
import com.vaadin.hilla.nonnullapi.NonNullApiProperties;
import com.vaadin.hilla.nonnullapi.NonNullableEntity;
import com.vaadin.hilla.nullmarked.NullMarkedEntity;
import com.vaadin.hilla.nullmarked.NullMarkedProperties;

public class ExplicitNullableTypeCheckerTest {
    private ExplicitNullableTypeChecker explicitNullableTypeChecker;
    private ExplicitNullableTypeCheckerHelper helper;
    private Type stringListType;
    private Type stringToDateMapType;
    private Type stringArrayType;

    @Before
    public void setup() throws NoSuchMethodException {
        explicitNullableTypeChecker = new ExplicitNullableTypeChecker();
        helper = new ExplicitNullableTypeCheckerHelper();

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
        Assert.assertNull(
                explicitNullableTypeChecker.checkValueForType(0, byte.class));
        Assert.assertNull(
                explicitNullableTypeChecker.checkValueForType(0, short.class));
        Assert.assertNull(
                explicitNullableTypeChecker.checkValueForType(0, int.class));
        Assert.assertNull(
                explicitNullableTypeChecker.checkValueForType(0L, long.class));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0.0f,
                float.class));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(0.0d,
                double.class));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(true,
                boolean.class));
        Assert.assertNull(
                explicitNullableTypeChecker.checkValueForType('a', char.class));
    }

    @Test
    public void should_ReturnNull_When_GivenNullValue_ForVoidType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(null,
                void.class));
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(null,
                Void.class));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForStringType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType("",
                String.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForStringType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                String.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("String"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForDateType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new Date(), Date.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForDateType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Date.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Date"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForDateTimeType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(LocalDateTime.now(), LocalDateTime.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForDateTimeType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                LocalDateTime.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("LocalDateTime"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForLocalTimeType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(LocalTime.now(), LocalTime.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForLcalTimeType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                LocalTime.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("LocalTime"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForMapType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new HashMap<String, String>(), Map.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForMapType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Map.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Map"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForObjectType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new Object(), Object.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForObjectType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Object.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Object"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForOptionalType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(Optional.empty(), Optional.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForOptionalType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                Optional.class);

        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("null"));
        Assert.assertTrue(error.contains("Optional.empty"));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValue_ForCollectionType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(new ArrayList<String>(), stringListType));
    }

    @Test
    public void should_ReturnError_When_GivenNullValue_ForCollectionType() {
        String error = explicitNullableTypeChecker.checkValueForType(null,
                stringListType);

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

        verify(checker).checkValueForType(eq("foo"), eq(String.class), any(),
                anyBoolean());
        verify(checker).checkValueForType(eq("bar"), eq(String.class), any(),
                anyBoolean());
    }

    private static class MyList extends ArrayList<String> {
    }

    @Test
    public void should_Recognize_Extended_List() {
        ExplicitNullableTypeCheckerHelper checker = spy(helper);

        MyList list = new MyList();
        list.add("foo");
        list.add("bar");

        checker.checkValueForType(list, MyList.class);
        verify(checker).checkValueForType(list, MyList.class);

        verify(checker).checkValueForType(eq("foo"), eq(String.class), any(),
                anyBoolean());
        verify(checker).checkValueForType(eq("bar"), eq(String.class), any(),
                anyBoolean());
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullItems_InListType() {
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(Arrays.asList("", ""), stringListType));
    }

    @Test
    public void should_ReturnError_When_GivenNullItem_InListType() {
        String error = explicitNullableTypeChecker
                .checkValueForType(Arrays.asList("", null, ""), stringListType);

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

        verify(checker).checkValueForType(eq(dateOne), eq(Date.class), any(),
                anyBoolean());
        verify(checker).checkValueForType(eq(dateTwo), eq(Date.class), any(),
                anyBoolean());
    }

    @Test
    public void should_ReturnNull_When_GivenNonNullValues_InMapType() {
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(
                parametrizedMapMethod(new Date(), new Date()),
                stringToDateMapType));
    }

    @Test
    public void should_ReturnError_When_GivenNullValues_InMapType() {
        String error = explicitNullableTypeChecker.checkValueForType(
                parametrizedMapMethod(new Date(), null), stringToDateMapType);

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

        verify(checker).checkValueForType(eq("foo"), eq(String.class), any(),
                anyBoolean());
        verify(checker).checkValueForType(eq("bar"), eq(String.class), any(),
                anyBoolean());
    }

    @Test
    public void should_ReturnNull_When_GivenNonNull_BeanProperties() {
        final Bean bean = new Bean();
        bean.setTitle("foo");

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(bean,
                Bean.class));
    }

    @Test
    public void should_ReturnError_When_GivenNull_BeanProperty() {
        // Property 'title' is null
        Bean bean = new Bean();

        String error = explicitNullableTypeChecker.checkValueForType(bean,
                Bean.class);

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

        verify(checker).checkValueForType(eq("foo"), eq(String.class), any(),
                anyBoolean());
        // Should not check non-bean properties
        verify(checker, never()).checkValueForType(eq("bar"), any(), any(),
                anyBoolean());
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
    public void should_ReturnError_When_AnnotatedNonnullAndGivenNull()
            throws NoSuchMethodException {
        Method method = getClass().getMethod("stringNonnull");

        Assert.assertNull("Should allow values with @NonNull",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        "someValue", method, false));
        Assert.assertNotNull("Should reject null with @NonNull",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(null,
                        method, false));
    }

    @Test
    public void should_ReturnNull_When_GivenNonNull_Generic_BeanProperties() {
        final Person person = new Person();
        person.setName("foo");
        person.setId(1);

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(person,
                Person.class));
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
                .checkValueForType(employee, Employee.class));

        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(company,
                Company.class));
    }

    @Test
    public void should_ReturnNull_When_GivenNullAndNotRequiredByContext() {
        Assert.assertNotNull(explicitNullableTypeChecker.checkValueForType(null,
                Employee.class));
    }

    @Test
    public void should_ReturnError_When_GivenNullAndRequiredByContext() {
        Assert.assertNotNull(explicitNullableTypeChecker.checkValueForType(null,
                Employee.class));
    }

    @Test
    public void should_NotReturnError_When_GivenNestedNullAndRequiredByTopContext() {
        Employee employee = new Employee();
        employee.setId(12);
        employee.setCompany(null);
        Assert.assertNull(explicitNullableTypeChecker
                .checkValueForType(employee, Employee.class));
    }

    @Test
    public void should_ReturnError_When_GivenNestedNullAndRequiredByItsOwnContext() {
        NonNullableEntity nonNullableEntity = new NonNullableEntity();
        nonNullableEntity.setValue(null);
        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForType(nonNullableEntity, NonNullableEntity.class));
    }

    @Test
    public void should_FollowNestedNullability_When_RequiredByNullMarkedPackage() {
        NullMarkedEntity nullMarkedEntity = new NullMarkedEntity();
        nullMarkedEntity.setValue("value");
        nullMarkedEntity.setNullableValue(null);
        Assert.assertNull("Nullable property should allow null value",
                explicitNullableTypeChecker.checkValueForType(nullMarkedEntity,
                        NullMarkedEntity.class));

        nullMarkedEntity.getNullableItems().add(null);
        Assert.assertNull("Nullable list item should allow null value",
                explicitNullableTypeChecker.checkValueForType(nullMarkedEntity,
                        NullMarkedEntity.class));

        nullMarkedEntity.getNullableValues().put("key", null);
        Assert.assertNull("Nullable map value should allow null value",
                explicitNullableTypeChecker.checkValueForType(nullMarkedEntity,
                        NullMarkedEntity.class));

        nullMarkedEntity.getItems().add(null);
        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForType(nullMarkedEntity, NullMarkedEntity.class));

        nullMarkedEntity.getItems().clear();
        nullMarkedEntity.getValues().put("key", null);
        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForType(nullMarkedEntity, NullMarkedEntity.class));

        nullMarkedEntity.getValues().clear();
        nullMarkedEntity.setValue(null);
        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForType(nullMarkedEntity, NullMarkedEntity.class));
    }

    @Test
    public void should_FollowGetterAndSetterAnnotations_When_RequiredByNullMarkedPackage() {
        NullMarkedProperties properties = new NullMarkedProperties();
        properties.setNullableGetter(null);
        Assert.assertNull("Nullable getter should allow null value",
                checkNullMarkedProperties(properties));

        properties = new NullMarkedProperties();
        properties.setNullableSetter(null);
        Assert.assertNull("Nullable setter parameter should allow null value",
                checkNullMarkedProperties(properties));

        properties = new NullMarkedProperties();
        properties.getNullableGetterItems().add(null);
        Assert.assertNull("Nullable getter list item should allow null value",
                checkNullMarkedProperties(properties));

        properties = new NullMarkedProperties();
        properties.getNullableSetterItems().add(null);
        Assert.assertNull(
                "Nullable setter parameter list item should allow null value",
                checkNullMarkedProperties(properties));

    }

    @Test
    public void should_ReturnError_When_GetterOrSetterAnnotationsDoNotAllowNull() {
        NullMarkedProperties properties = new NullMarkedProperties();
        properties.setNullableGetterNonnullSetter(null);
        Assert.assertNotNull("Nonnull should take precedence over nullable",
                checkNullMarkedProperties(properties));

        properties = new NullMarkedProperties();
        properties.getNullableFieldOfOtherTypeItems().add(null);
        Assert.assertNotNull(
                "Nullable list item of a field with another type than the "
                        + "getter should be ignored",
                checkNullMarkedProperties(properties));
    }

    @Test
    public void should_FollowGetterAndSetterAnnotations_When_RequiredByNonNullApiPackage() {
        NonNullApiProperties properties = new NonNullApiProperties();
        properties.setNullableGetter(null);
        Assert.assertNull("Nullable getter should allow null value",
                checkNonNullApiProperties(properties));

        properties = new NonNullApiProperties();
        properties.setNullableSetter(null);
        Assert.assertNull("Nullable setter parameter should allow null value",
                checkNonNullApiProperties(properties));
    }

    private String checkNonNullApiProperties(NonNullApiProperties properties) {
        return explicitNullableTypeChecker.checkValueForType(properties,
                NonNullApiProperties.class);
    }

    private String checkNullMarkedProperties(NullMarkedProperties properties) {
        return explicitNullableTypeChecker.checkValueForType(properties,
                NullMarkedProperties.class);
    }

    @Test
    public void should_ReturnError_When_NonnullGetterAndBeanNotRequiredByContext() {
        NonnullGetterBean bean = new NonnullGetterBean();
        bean.setName("foo");
        Assert.assertNull(explicitNullableTypeChecker.checkValueForType(bean,
                NonnullGetterBean.class));

        bean.setName(null);
        String error = explicitNullableTypeChecker.checkValueForType(bean,
                NonnullGetterBean.class);
        Assert.assertNotNull(error);
        Assert.assertTrue(error.contains("property 'name'"));
    }

    @Test
    public void should_FollowNestedTypeAnnotations_When_BeanNotRequiredByContext() {
        UnmarkedListBean bean = new UnmarkedListBean();
        bean.getItems().add(null);
        Assert.assertNull("List item without @NonNull should allow null value",
                explicitNullableTypeChecker.checkValueForType(bean,
                        UnmarkedListBean.class));

        bean.getNonNullItems().add(null);
        Assert.assertNotNull(explicitNullableTypeChecker.checkValueForType(bean,
                UnmarkedListBean.class));
    }

    @Test
    public void should_FollowNestedTypeAnnotations_When_RequiredByContext()
            throws NoSuchMethodException {
        Assert.assertNull("Nullable list item should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        Arrays.asList("foo", null),
                        getClass().getMethod("nullableItemList"), true));
        Assert.assertNull("Nullable map value should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        parametrizedMapMethod(new Date(), null),
                        getClass().getMethod("nullableValueMap"), true));
        Assert.assertNull("Nullable array item should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        new String[] { "foo", null },
                        getClass().getMethod("nullableItemArray"), true));
        Assert.assertNull("Nullable array should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(null,
                        getClass().getMethod("nullableArray"), true));

        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForAnnotatedElement(Arrays.asList("foo", null),
                        getClass().getMethod("parametrizedListMethod",
                                String[].class),
                        true));

        List<List<String>> nestedList = List.of(Arrays.asList("foo", null));
        Assert.assertNull("Nullable nested list item should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        nestedList, getClass().getMethod("nullableNestedList"),
                        true));
        Assert.assertNotNull(
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        nestedList, getClass().getMethod("nestedList"), true));
    }

    @Test
    public void should_FollowNestedTypeAnnotations_When_NotRequiredByContext()
            throws NoSuchMethodException {
        Assert.assertNull("List item without @Nonnull should allow null value",
                explicitNullableTypeChecker.checkValueForAnnotatedElement(
                        Arrays.asList("foo", null),
                        getClass().getMethod("hillaNonnullList"), false));

        Assert.assertNotNull(explicitNullableTypeChecker
                .checkValueForAnnotatedElement(Arrays.asList("foo", null),
                        getClass().getMethod("hillaNonnullItemList"), false));
    }

    @Test
    public void should_ReturnNull_When_ReturnTypeAnnotatedNullableAndRequiredByContext()
            throws NoSuchMethodException {
        String error = explicitNullableTypeChecker
                .checkValueForAnnotatedElement(null,
                        getClass().getMethod("stringTypeUseNullable"), true);

        Assert.assertNull(
                "Type-use nullable return type should allow null value", error);
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
    public @org.jspecify.annotations.Nullable String stringTypeUseNullable() {
        return "";
    }

    /**
     * Method for testing
     */
    public List<@org.jspecify.annotations.Nullable String> nullableItemList() {
        return List.of();
    }

    /**
     * Method for testing
     */
    public Map<String, @org.jspecify.annotations.Nullable Date> nullableValueMap() {
        return Map.of();
    }

    /**
     * Method for testing
     */
    public String @org.jspecify.annotations.Nullable [] nullableArray() {
        return new String[0];
    }

    /**
     * Method for testing
     */
    public @org.jspecify.annotations.Nullable String[] nullableItemArray() {
        return new String[0];
    }

    /**
     * Method for testing
     */
    public List<List<@org.jspecify.annotations.Nullable String>> nullableNestedList() {
        return List.of();
    }

    /**
     * Method for testing
     */
    public List<List<String>> nestedList() {
        return List.of();
    }

    /**
     * Method for testing
     */
    public @com.vaadin.hilla.Nonnull List<String> hillaNonnullList() {
        return List.of();
    }

    /**
     * Method for testing
     */
    public @com.vaadin.hilla.Nonnull List<@com.vaadin.hilla.Nonnull String> hillaNonnullItemList() {
        return List.of();
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
    @jakarta.annotation.Nonnull
    public String stringNonnull() {
        return "";
    }

    static private class NonnullGetterBean {
        private String name;

        public @NonNull String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static private class UnmarkedListBean {
        private @NonNull List<String> items = new ArrayList<>();
        private @NonNull List<@NonNull String> nonNullItems = new ArrayList<>();

        public List<String> getItems() {
            return items;
        }

        public List<String> getNonNullItems() {
            return nonNullItems;
        }
    }

    static private class Bean {
        static String staticProperty;
        @JsonIgnore
        public String ignoreProperty;
        public String description;
        transient String transientProperty;
        @NonNull
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
