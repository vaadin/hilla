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
package com.vaadin.hilla.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.TypeIdentityEndpoint;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * The model is built from the Java classes, so it knows which type a value
 * comes from even where TypeScript writes them all the same way. Nothing else
 * pins this: the generated TypeScript cannot tell a date from an instant, or a
 * long from a double.
 */
public class TypeIdentityTest {
    private final EndpointModel endpoint = new FullStackGenerator(
            TypeIdentityTest.class, TypeIdentityEndpoint.class).parseModel()
            .get(0);
    private final List<MethodModel> methods = endpoint.methods();

    @Test
    public void should_TellWhichClassTheEndpointComesFrom() {
        assertEquals(TypeIdentityEndpoint.class.getSimpleName(),
                endpoint.name());
        assertEquals(TypeIdentityEndpoint.class.getName(),
                endpoint.javaClass());
    }

    @Test
    public void should_TellApartTheTypesWrittenAsAString() {
        assertEquals(
                List.of("approximated: number (float)", "counted: number (int)",
                        "date: string (java.time.LocalDate)",
                        "enabled: boolean (boolean)",
                        "exact: number (java.math.BigDecimal)",
                        "identifier: number (long)", "initial: string (char)",
                        "instant: string (java.time.Instant)",
                        "measured: number (double)",
                        "moment: string (java.time.LocalDateTime)",
                        "text: string (java.lang.String)"),
                methods.stream().map(TypeIdentityTest::describe).sorted()
                        .collect(Collectors.toList()));
    }

    private static String describe(MethodModel method) {
        var scalar = (TypeModel.Scalar) method.returnType();
        return method.name() + ": "
                + scalar.kind().name().toLowerCase(java.util.Locale.ROOT) + " ("
                + scalar.javaType() + ")";
    }
}
