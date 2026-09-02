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
package com.vaadin.hilla.generator.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.SampleEndpoint;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.MethodModel;
import com.vaadin.hilla.generator.model.ParameterModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * Verifies the model built for a browser callable class, which is what the
 * TypeScript is written from.
 */
public class OpenAPIToModelTest {
    private final EndpointModel endpoint = OpenAPIToModel
            .endpoints(new FullStackGenerator(OpenAPIToModelTest.class,
                    SampleEndpoint.class).parse())
            .get(0);

    @Test
    public void should_NameTheEndpointAndTheClassItComesFrom() {
        assertEquals("SampleEndpoint", endpoint.name());
        assertEquals(SampleEndpoint.class.getName(), endpoint.javaClass());
        assertEquals(
                List.of("count", "counts", "describe", "find", "greet", "names",
                        "ping", "shadow"),
                endpoint.methods().stream().map(MethodModel::name).toList());
    }

    @Test
    public void should_TellApartWhatCanBeAbsentAndWhatCannot() {
        // A primitive always has a value, a boxed type or a reference does not
        assertEquals(new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER, false),
                method("count").returnType());
        assertEquals(new TypeModel.Scalar(TypeModel.ScalarKind.STRING, true),
                method("greet").returnType());
    }

    @Test
    public void should_KeepTheMethodWithoutAReturnValueApart() {
        assertEquals(TypeModel.Scalar.of(TypeModel.ScalarKind.VOID),
                method("ping").returnType());
    }

    @Test
    public void should_BuildTheStructureOfCompoundTypes() {
        assertEquals(new TypeModel.ArrayOf(
                new TypeModel.Scalar(TypeModel.ScalarKind.STRING, true), true),
                method("names").returnType());
        assertEquals(new TypeModel.MapOf(
                new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER, true), true),
                method("counts").returnType());
    }

    @Test
    public void should_ReferToTheClassAnEntityComesFrom() {
        assertEquals(
                new TypeModel.EntityRef(SampleEndpoint.Sample.class.getName(),
                        List.of(), true),
                method("find").returnType());
    }

    @Test
    public void should_KeepTheParametersInOrderWithTheirNames() {
        var parameters = method("describe").parameters();

        assertEquals(List.of("firstName", "lastName", "age"),
                parameters.stream().map(ParameterModel::name).toList());
        assertEquals(
                List.of(new TypeModel.Scalar(TypeModel.ScalarKind.STRING, true),
                        new TypeModel.Scalar(TypeModel.ScalarKind.STRING, true),
                        new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER,
                                false)),
                parameters.stream().map(ParameterModel::type).toList());
    }

    private MethodModel method(String name) {
        return endpoint.methods().stream()
                .filter(method -> method.name().equals(name)).findFirst()
                .orElseThrow();
    }
}
