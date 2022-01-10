package com.vaadin.fusion.generator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vaadin.fusion.utils.TestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BarrelGeneratorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private BarrelGenerator barrelGenerator;

    @Before
    public void init() {
        barrelGenerator = new BarrelGenerator(
                temporaryFolder.getRoot().toPath());
    }

    @Test
    public void should_generateBarrel() throws Exception {
        OpenAPI openAPI = createOpenAPI("FistEndpoint", "SecondEndpoint");
        barrelGenerator.generate(openAPI);

        Path outputPath = barrelGenerator.getOutputFilePath();

        assertTrue(outputPath.toFile().exists());
        String expected = TestUtils
                .readResource(getClass().getResource("expected-barrel.ts"));
        String actual = StringUtils.toEncodedString(
                Files.readAllBytes(outputPath), StandardCharsets.UTF_8).trim();

        TestUtils.equalsIgnoreWhiteSpaces(expected, actual);
    }

    @Test
    public void should_not_generateBarrel_When_NoEndpointsSpecified() {
        OpenAPI openAPI = createOpenAPI();
        barrelGenerator.generate(openAPI);

        assertFalse(barrelGenerator.getOutputFilePath().toFile().exists());
    }

    private OpenAPI createOpenAPI(String... names) {
        OpenAPI openAPI = new OpenAPI();

        for (String name : names) {
            openAPI.addTagsItem(new Tag().name(name));
        }

        return openAPI;
    }
}
