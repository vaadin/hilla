/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator.tsmodel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.server.connect.generator.endpoints.AbstractEndpointGeneratorBaseTest;
import com.vaadin.flow.server.connect.generator.tsmodel.TsFormEndpoint.MyEntity;
import com.vaadin.flow.server.connect.generator.tsmodel.TsFormEndpoint.MyEntityId;

import elemental.json.JsonObject;
import static com.vaadin.flow.server.connect.generator.OpenApiObjectGenerator.CONSTRAINT_ANNOTATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TsFormTest extends AbstractEndpointGeneratorBaseTest {

    public TsFormTest() {
        super(Collections.singletonList(TsFormEndpoint.class));
    }

    @Test
    public void should_addEntityJavaAnnotations_toOpenApi() {
        generateOpenApi(null);

        JsonObject apiJson = readJsonFile(openApiJsonOutput);

        String modelName = MyEntity.class.getName().replace('$', '.');

        JsonObject props = apiJson.getObject("components").getObject("schemas")
                .getObject(modelName).getArray("allOf").getObject(1)
                .getObject("properties");

        assertFalse(props.getObject("foo").hasKey(CONSTRAINT_ANNOTATIONS));
        assertEquals("AssertFalse()", props.getObject("assertFalse")
                .getArray(CONSTRAINT_ANNOTATIONS).getString(0));
        assertEquals("AssertTrue()", props.getObject("assertTrue")
                .getArray(CONSTRAINT_ANNOTATIONS).getString(0));
        assertEquals("Digits({integer:5, fraction:2})",
                props.getObject("digits").getArray(CONSTRAINT_ANNOTATIONS)
                        .getString(0));
        assertEquals("NotEmpty()", props.getObject("notEmpty")
                .getArray(CONSTRAINT_ANNOTATIONS).getString(0));
        assertEquals("NotNull()", props.getObject("notEmpty")
                .getArray(CONSTRAINT_ANNOTATIONS).getString(1));
        assertEquals("NotNull()", props.getObject("notNullEntity")
                .getArray(CONSTRAINT_ANNOTATIONS).getString(0));
    }

    @Test
    public void should_generate_FormModels() throws IOException {
        generateOpenApi(null);

        generateTsEndpoints();

        String entityIdPath = MyEntityId.class.getName().replaceAll("[\\.\\$]",
                "/");
        String entityPath = MyEntity.class.getName().replaceAll("[\\.\\$]",
                "/");

        File entityIdFile = new File(outputDirectory.getRoot(),
                entityIdPath + ".ts");
        File formModelIdFile = new File(outputDirectory.getRoot(),
                entityIdPath + "Model.ts");
        File entityFile = new File(outputDirectory.getRoot(),
                entityPath + ".ts");
        File formModelFile = new File(outputDirectory.getRoot(),
                entityPath + "Model.ts");

        assertTrue(entityIdFile.exists());
        assertTrue(formModelIdFile.exists());
        assertTrue(entityFile.exists());
        assertTrue(formModelFile.exists());

        final List<String> content = Files.lines(formModelFile.toPath())
                .collect(Collectors.toList());
        final List<String> expected = Files.lines(new File(
                getClass().getResource("expected-TsFormEndpoint.ts").getFile())
                        .toPath())
                .collect(Collectors.toList());

        // Path separators for files need to be changed on windows.
        content.replaceAll(line -> {
            if (line.contains("file://")) {
                return line.replace('\\', '/').replaceAll(
                        "file://.*/fusion-endpoint",
                        "file:///.../fusion-endpoint");
            }
            return line;
        });
        assertEquals("Rows in generated and expected files differ",
                expected.size(), content.size());

        int line = 0;
        List<String> faultyLines = new ArrayList<>();
        for (String expectedLine : expected) {
            String actualLine = content.get(line);
            // ignore the line for file reference, as the file path syntax is
            // different on Windows
            if (isFileRefenreceLine(expectedLine)) {
                Assert.assertTrue("should have the file reference",
                        isFileRefenreceLine(actualLine));
            } else if (!expectedLine.equals(actualLine)) {
                faultyLines.add(String.format("L%d :: expected: [%s] got [%s]",
                        line + 1, expectedLine, actualLine));
            }
            line++;
        }
        assertTrue(
                "Found differences in generated file: " + faultyLines.stream()
                        .collect(Collectors.joining("\n")),
                faultyLines.isEmpty());
    }

    private boolean isFileRefenreceLine(String line) {
        return line.contains("@see {@link file:");
    }
}
