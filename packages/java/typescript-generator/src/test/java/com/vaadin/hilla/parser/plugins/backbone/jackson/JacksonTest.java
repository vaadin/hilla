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
package com.vaadin.hilla.parser.plugins.backbone.jackson;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.plugins.backbone.jackson.JacksonEndpoint.Sample;
import com.vaadin.hilla.parser.testutils.AbstractFullStackTest;

public class JacksonTest extends AbstractFullStackTest {
    @Test
    public void should_CorrectlyIgnoreFieldsBasedOnJSONAnnotations() {
        // This test is only run on JDKs that return fields and methods in the
        // order they are defined. Some JDKs like JetBrains Runtime does not
        Assumptions.assumeTrue(fieldsReturnedInDefinedOrder(),
                "This test is skipped on JDKs that do not return declared methods in the file order");

        assertTypescriptMatchesSnapshot(JacksonEndpoint.class);
    }

    private boolean fieldsReturnedInDefinedOrder() {
        List<String> methodsInClass = List.of("getPrivateProp",
                "getPrivatePropWithJsonIgnore",
                "getPrivatePropWithJsonIgnoreProperties",
                "getPrivateTransientPropWithGetter", "getPropertyGetterOnly",
                "setPropertyGetterOnly", "getPropertyWithDifferentField",
                "setPropertyWithDifferentField", "getRenamedPrivateProp",
                "setPropertySetterOnly");

        List<String> declaredMethods = Stream
                .of(Sample.class.getDeclaredMethods()).map(Method::getName)
                .filter(name -> !name.equals("$jacocoInit")).toList();

        if (declaredMethods.size() != methodsInClass.size()) {
            throw new IllegalStateException(methodsInClass.size()
                    + " methods defined, " + declaredMethods.size() + " found. "
                    + "If you modify methods in the " + Sample.class.getName()
                    + " you need to update this method");
        }

        return declaredMethods.equals(methodsInClass);
    }
}
