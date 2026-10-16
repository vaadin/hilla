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
package com.vaadin.hilla.generator.typescript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.model.AnnotationModel;
import com.vaadin.hilla.generator.model.ConstraintModel;
import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.PropertyModel;
import com.vaadin.hilla.generator.model.TypeModel;
import com.vaadin.hilla.parser.plugins.model.JvmTypeRef;

public class ModelWriterTest {
    private static final TypeModel.Scalar STRING = new TypeModel.Scalar(
            TypeModel.ScalarKind.STRING, false, "java.lang.String");

    private final ModelWriter writer = new ModelWriter(new ImportRegistry(),
            "");

    @Test
    public void should_TellAModelTheJavaTypeOfAPrimitiveOrOfTheJDK() {
        assertEquals(
                "m.meta(m.optional(m.array(m.meta(NumberModel, { jvmType: 'int' }))), { jvmType: 'int[]' })",
                writer.model(new TypeModel.ArrayOf(
                        new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER, false,
                                "int"),
                        true, "int[]")));
        assertEquals(
                "m.meta(m.optional(m.record(m.meta(StringModel, { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.Map' })",
                writer.model(
                        new TypeModel.MapOf(STRING, true, "java.util.Map")));
    }

    @Test
    public void should_LeaveOutTheJavaTypeOfAClassOfTheApplication() {
        // Neither an array of a class of the application, nor a collection
        // the application declares, nor an array of arrays is told about,
        // which is how the Node generator left them
        for (var javaType : new String[] { "com.example.Item[]",
                "com.example.Items", "int[][]" }) {
            assertEquals(
                    "m.optional(m.array(m.meta(StringModel, { jvmType: 'java.lang.String' })))",
                    writer.model(new TypeModel.ArrayOf(STRING, true, javaType)),
                    javaType);
        }
    }

    @Test
    public void should_ConstrainAModelOnlyWithWhatTheModelLibraryAcceptsOnIt() {
        // A constraint the model library does not accept on the kind of model
        // would throw as the module is evaluated, and one it does not export
        // would not compile. The binder has no validator for PastOrPresent and
        // FutureOrPresent at all. An attribute the constraint does not declare
        // is left out, since it takes exactly the ones it declares
        var constraints = List.of(ConstraintModel.of("AssertTrue"),
                ConstraintModel.of("PastOrPresent"),
                ConstraintModel.of("FutureOrPresent"),
                ConstraintModel.of("Unheard"),
                new ConstraintModel("Pattern",
                        Map.of("regexp", "[a-z]+", "flags",
                                List.of("CASE_INSENSITIVE"))),
                new ConstraintModel("Size", Map.of("min", 1)));

        assertEquals(
                "m.meta(m.constrained(StringModel, Pattern({ regexp: '[a-z]+' }), Size({ min: 1 })), { jvmType: 'java.lang.String' })",
                writer.model(new TypeModel.Scalar(TypeModel.ScalarKind.STRING,
                        false, "java.lang.String", constraints, List.of())));
        assertEquals(
                "m.constrained(m.optional(m.array(StringModel)), Size({ min: 1 }))",
                writer.model(new TypeModel.ArrayOf(
                        new TypeModel.Scalar(TypeModel.ScalarKind.STRING, false,
                                "com.example.Code"),
                        true, "com.example.Codes", constraints, List.of())));
    }

    @Test
    public void should_TellAModelWhatTheAnnotationsOfTheValueSay() {
        // A class is written as the reference to it the model library reads,
        // and the attributes go in the order the annotation gives them
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("targetEntity", new JvmTypeRef("com.example.Item"));
        attributes.put("cascade", List.of("PERSIST", "MERGE"));

        assertEquals(
                "m.meta(m.optional(ItemModel), { annotations: [{ jvmType: 'jakarta.persistence.OneToMany', attributes: { targetEntity: { jvmType: 'com.example.Item' }, cascade: ['PERSIST', 'MERGE'] } }, { jvmType: 'jakarta.persistence.Id' }] })",
                writer.model(new TypeModel.EntityRef("com.example.Item",
                        List.of(), true, List.of(),
                        List.of(new AnnotationModel(
                                "jakarta.persistence.OneToMany", attributes),
                                AnnotationModel
                                        .of("jakarta.persistence.Id")))));
    }

    @Test
    public void should_ReadAModelWhichRefersBackLazily() {
        // A refers to B, which refers back to A, and to C, which does not.
        // Only the reference taking part in the cycle is read lazily, and
        // the reference of a model to itself needs no import at all
        var a = bean("com.example.A", TypeModel.EntityRef.of("com.example.B"),
                TypeModel.EntityRef.of("com.example.A"));
        var b = bean("com.example.B", TypeModel.EntityRef.of("com.example.A"),
                TypeModel.EntityRef.of("com.example.C"));
        var c = bean("com.example.C");
        var cycles = ModelCycles.of(List.of(a, b, c));
        var imports = new ImportRegistry();
        var models = new ModelWriter(imports, "com/example", "com.example.B",
                cycles);

        assertEquals("m.lazy((): ObjectModel<A> => AModel)",
                models.model(TypeModel.EntityRef.of("com.example.A")));
        assertEquals("CModel",
                models.model(TypeModel.EntityRef.of("com.example.C")));
        assertEquals("m.self",
                models.model(TypeModel.EntityRef.of("com.example.B")));
        assertEquals(List.of(
                "import m, { ObjectModel } from '@vaadin/hilla-models';",
                "import type A from './A.js';",
                "import AModel from './AModel.js';",
                "import CModel from './CModel.js';"), imports.write());
    }

    private static EntityModel bean(String javaClass, TypeModel... types) {
        var properties = new ArrayList<PropertyModel>();

        for (var index = 0; index < types.length; index++) {
            properties.add(new PropertyModel("p" + index, types[index]));
        }

        return new EntityModel.Bean(javaClass, List.of(), List.of(),
                properties);
    }
}
