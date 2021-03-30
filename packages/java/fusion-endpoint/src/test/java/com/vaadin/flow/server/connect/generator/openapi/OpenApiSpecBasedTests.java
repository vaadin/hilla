/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.server.connect.generator.openapi;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.vaadin.flow.server.connect.generator.TestUtils;
import com.vaadin.flow.server.connect.generator.VaadinConnectClientGenerator;
import com.vaadin.flow.server.connect.generator.VaadinConnectTsGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static com.vaadin.flow.server.connect.generator.TestUtils.equalsIgnoreWhiteSpaces;

public class OpenApiSpecBasedTests {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
    }

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    @Test
    public void should_NotGenerateOutput_When_NoOpenApiInput()
            throws Exception {
        String fileName = "whatever";

        File output = outputDirectory.newFolder();

        assertTrue(output.exists());

        VaadinConnectTsGenerator.launch(new File(fileName), output);

        assertFalse(output.exists());
    }

    @Test
    public void should_ThrowError_WhenOpenAPIHasNoDescriptionInResponse() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("description is missing");

        VaadinConnectTsGenerator.launch(
                getResourcePath("no-description-response-openapi.json"),
                outputDirectory.getRoot());
    }

    @Test
    public void should_ThrowError_WhenOpenAPIHasInvalidTypeReference() {
        String fileName = "invalid-schema-type-openapi.json";

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(fileName);

        VaadinConnectTsGenerator.launch(getResourcePath(fileName),
                outputDirectory.getRoot());
    }

    // The swagger codegen catches all the exceptions and rethrows with
    // RuntimeException
    @Test
    public void should_ThrowException_When_PathHasTrailingSlash() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Could not process operation");

        VaadinConnectTsGenerator.launch(
                getResourcePath("wrong-input-path-openapi.json"),
                outputDirectory.getRoot());
    }

    @Test
    public void should_ThrowException_When_JsonHasGetOperation() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Could not process operation");

        VaadinConnectTsGenerator.launch(
                getResourcePath("get-operation-openapi.json"),
                outputDirectory.getRoot());
    }

    @Test
    public void should_RemoveStaleGeneratedFiles_When_OpenAPIInputChanges() {
        Path defaultConnectClient = Paths.get(
                outputDirectory.getRoot().getAbsolutePath(),
                VaadinConnectClientGenerator.CONNECT_CLIENT_NAME);
        VaadinConnectClientGenerator vaadinConnectClientGenerator = new VaadinConnectClientGenerator(
                new Properties());
        // First generating round
        vaadinConnectClientGenerator
                .generateVaadinConnectClientFile(defaultConnectClient);
        VaadinConnectTsGenerator.launch(
                getResourcePath(
                        "esmodule-generator-TwoEndpointsThreeMethods.json"),
                outputDirectory.getRoot());
        assertEquals(
                "Expect to have 2 generated TS files and a connect-client.default.ts",
                3, outputDirectory.getRoot().list().length);
        // Second generating round
        VaadinConnectTsGenerator.launch(new File(getClass()
                .getResource("esmodule-generator-OneEndpointOneMethod.json")
                .getPath()), outputDirectory.getRoot());
        assertEquals(
                "Expected to have 1 generated TS files and a connect-client.default.ts",
                2, outputDirectory.getRoot().list().length);

        assertClassGeneratedTs("FooBarEndpoint");
    }

    @Test
    public void should_GenerateNoTsDoc_When_JsonHasNoTsDocOperation()
            throws Exception {
        VaadinConnectTsGenerator.launch(
                getResourcePath("no-tsdoc-operation.json"),
                outputDirectory.getRoot());

        String actual = readFileInTempDir("GeneratorTestClass.ts");

        String expected = TestUtils
                .readResource(getClass().getResource("expected-no-tsdoc.ts"));
        equalsIgnoreWhiteSpaces(expected, actual);
    }

    @Test
    public void should_GeneratePartlyTsDoc_When_JsonHasParametersAndReturnType()
            throws Exception {
        VaadinConnectTsGenerator.launch(
                getResourcePath("parameters-and-return-tsdoc.json"),
                outputDirectory.getRoot());

        String actual = readFileInTempDir("GeneratorTestClass.ts");

        String expected = TestUtils.readResource(
                getClass().getResource("expected-partly-tsdoc.ts"));

        equalsIgnoreWhiteSpaces(expected, actual);
    }

    @Test
    public void should_GenerateTwoClasses_When_OperationContainsTwoTags()
            throws Exception {
        VaadinConnectTsGenerator.launch(
                getResourcePath("multiple-tags-operation.json"),
                outputDirectory.getRoot());
        Path firstOutputFilePath = outputDirectory.getRoot().toPath()
                .resolve("MyFirstTsClass.ts");
        Path secondOutputFilePath = outputDirectory.getRoot().toPath()
                .resolve("MySecondTsClass.ts");
        String firstActualTs = StringUtils
                .toEncodedString(Files.readAllBytes(firstOutputFilePath),
                        StandardCharsets.UTF_8)
                .trim().replace("\r", "");
        String secondActualTs = StringUtils
                .toEncodedString(Files.readAllBytes(secondOutputFilePath),
                        StandardCharsets.UTF_8)
                .trim().replace("\r", "");
        String expectedFirstClass = TestUtils.readResource(getClass()
                .getResource("expected-first-class-multiple-tags.ts"));
        String expectedSecondClass = TestUtils.readResource(getClass()
                .getResource("expected-second-class-multiple-tags.ts"));
        equalsIgnoreWhiteSpaces(expectedFirstClass, firstActualTs);
        equalsIgnoreWhiteSpaces(expectedSecondClass, secondActualTs);
    }

    @Test
    public void should_GenerateDefaultClass_When_OperationHasNoTag()
            throws Exception {
        VaadinConnectTsGenerator.launch(
                getResourcePath("no-tag-operation.json"),
                outputDirectory.getRoot());
        String actualTs = readFileInTempDir("Default.ts");
        String expectedFirstClass = TestUtils.readResource(
                getClass().getResource("expected-default-class-no-tag.ts"));
        equalsIgnoreWhiteSpaces(expectedFirstClass, actualTs);
    }

    @Test
    public void should_RenderMultipleLinesHTMLCorrectly_When_JavaDocHasMultipleLines()
            throws Exception {
        VaadinConnectTsGenerator.launch(
                getResourcePath("multiplelines-description.json"),
                outputDirectory.getRoot());
        String actualTs = readFileInTempDir("GeneratorTestClass.ts");
        String expectedTs = TestUtils.readResource(getClass()
                .getResource("expected-multiple-lines-description.ts"));
        equalsIgnoreWhiteSpaces(expectedTs, actualTs);
    }

    private String readFileInTempDir(String fileName) throws IOException {
        Path outputPath = outputDirectory.getRoot().toPath().resolve(fileName);
        return StringUtils.toEncodedString(Files.readAllBytes(outputPath),
                StandardCharsets.UTF_8);
    }

    private File getResourcePath(String resourceName) {
        return new File(getClass().getResource(resourceName).getPath());
    }

    private void assertClassGeneratedTs(String expectedClass) {
        Path outputFilePath = outputDirectory.getRoot().toPath()
                .resolve(expectedClass + ".ts");
        String actualTs;
        try {
            actualTs = StringUtils.toEncodedString(
                    Files.readAllBytes(outputFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String expectedTs = TestUtils.readResource(getClass()
                .getResource(String.format("expected-%s.ts", expectedClass)));

        equalsIgnoreWhiteSpaces(
                String.format("Class '%s' has unexpected json produced",
                        expectedClass),
                expectedTs, actualTs);
    }

}
