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
package com.vaadin.hilla.crud.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.vaadin.hilla.crud.JpaFilterConverter;
import com.vaadin.hilla.crud.TestEnum;
import com.vaadin.hilla.crud.TestObject;
import com.vaadin.hilla.crud.TestRepository;
import com.vaadin.hilla.crud.filter.PropertyStringFilter.Matcher;

@RunWith(SpringRunner.class)
@DataJpaTest()
public class FilterTransformerTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TestRepository repository;

    @Test
    public void testRemap() {
        var testObject = new TestObject();
        testObject.setName("test1");
        testObject.setIntValue(1);
        testObject.setEnumValue(TestEnum.TEST1);
        entityManager.persist(testObject);

        testObject = new TestObject();
        testObject.setName("other1");
        testObject.setIntValue(2);
        testObject.setEnumValue(TestEnum.TEST2);
        entityManager.persist(testObject);

        testObject = new TestObject();
        testObject.setName("test2");
        testObject.setIntValue(3);
        testObject.setEnumValue(TestEnum.TEST2);
        entityManager.persist(testObject);

        testObject = new TestObject();
        testObject.setName("test3");
        testObject.setIntValue(4);
        testObject.setEnumValue(TestEnum.TEST3);
        entityManager.persist(testObject);

        var nameFilter = new PropertyStringFilter("dtoName", Matcher.CONTAINS,
                "test");
        var intValueFilter = new PropertyStringFilter("dtoIntValue",
                Matcher.EQUALS, "1");
        var enumValueFilter = new PropertyStringFilter("dtoEnumValue",
                Matcher.EQUALS, null);

        var orFilter = new OrFilter(intValueFilter, enumValueFilter);
        var andFilter = new AndFilter(nameFilter, orFilter);

        var transformer = new FilterTransformer().withMapping("dtoName", "name")
                .withMapping("dtoIntValue", "intValue")
                .withMapping("dtoEnumValue", "enumValue")
                .withFilterTransformation(filter -> {
                    // this also tests that the filter transformation is applied
                    // after the mapping
                    if (filter.getPropertyId().equals("enumValue")) {
                        filter.setFilterValue("TEST3");
                    }
                    return filter;
                });

        var filter = transformer.apply(andFilter);
        var spec = JpaFilterConverter.toSpec(filter, TestObject.class);
        var result = repository.findAll(spec);
        assertEquals(2, result.size());

        var result1 = result.get(0);
        assertEquals("test1", result1.getName());
        assertEquals(1, result1.getIntValue());
        assertEquals(TestEnum.TEST1, result1.getEnumValue());

        var result2 = result.get(1);
        assertEquals("test3", result2.getName());
        assertEquals(4, result2.getIntValue());
        assertEquals(TestEnum.TEST3, result2.getEnumValue());
    }
}
