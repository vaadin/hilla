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

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.model.TypeModel;

public class ModelWriterTest {
    private final ModelWriter writer = new ModelWriter(new ImportRegistry(),
            "");

    @Test
    public void should_TellAModelTheJavaTypeOfAPrimitiveOrOfTheJDK() {
        assertEquals(
                """
                        new ArrayModel(parent, key, true,
                          (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: 'int' } }),
                          { meta: { javaType: 'int[]' } })""",
                writer.instance(new TypeModel.ArrayOf(
                        new TypeModel.Scalar(TypeModel.ScalarKind.NUMBER, false,
                                "int"),
                        true, "int[]")));
        assertEquals(
                "new ObjectModel(parent, key, true, { meta: { javaType: 'java.util.Map' } })",
                writer.instance(new TypeModel.MapOf(
                        new TypeModel.Scalar(TypeModel.ScalarKind.STRING, false,
                                "java.lang.String"),
                        true, "java.util.Map")));
    }

    @Test
    public void should_LeaveOutTheJavaTypeOfAClassOfTheApplication() {
        // Neither an array of a class of the application, nor a collection
        // the application declares, nor an array of arrays is told about,
        // which is how the Node generator left them
        var item = new TypeModel.Scalar(TypeModel.ScalarKind.STRING, false,
                "java.lang.String");

        for (var javaType : new String[] { "com.example.Item[]",
                "com.example.Items", "int[][]" }) {
            assertEquals(
                    """
                            new ArrayModel(parent, key, true,
                              (parent, key) => new StringModel(parent, key, false, { meta: { javaType: 'java.lang.String' } }))""",
                    writer.instance(
                            new TypeModel.ArrayOf(item, true, javaType)),
                    javaType);
        }
    }
}
