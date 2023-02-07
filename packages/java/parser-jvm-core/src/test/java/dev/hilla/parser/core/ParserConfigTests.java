package dev.hilla.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

public class ParserConfigTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Set<String> defaultClassPathElements;
    private String defaultEndpointAnnotationName;
    private String defaultEndpointExposedAnnotationName;
    private OpenAPI defaultOpenAPI;
    private Parser parser;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        defaultEndpointAnnotationName = "dev.hilla.Endpoint";

        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
        defaultEndpointExposedAnnotationName = "dev.hilla.EndpointExposed";
        defaultOpenAPI = new OpenAPI()
                .info(new Info().title("Vaadin Application").version("1.0.0"))
                .servers(List
                        .of(new Server().url("http://localhost:8080/connect")
                                .description("Vaadin Backend")))
                .paths(new Paths());
        parser = new Parser().classPath(defaultClassPathElements)
                .endpointAnnotation(defaultEndpointAnnotationName)
                .endpointExposedAnnotation(
                        defaultEndpointExposedAnnotationName);
    }

    @Test
    public void should_AllowAddingPluginsAsCollection() {
        var foo = new FooPlugin(1);
        var bar = new BarPlugin(0);
        var baz = new BazPlugin(-5);

        var config = parser.plugins(List.of(foo, bar, baz)).getConfig();

        assertEquals(List.of(baz, bar, foo),
                new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_AllowAddingPluginsAsVararg() {
        var foo = new FooPlugin(1);
        var bar = new BarPlugin(0);
        var baz = new BazPlugin(-5);

        var config = parser.plugins(foo, bar, baz).getConfig();

        assertEquals(List.of(baz, bar, foo),
                new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_AllowAddingPluginsOneByOne() {
        var foo = new FooPlugin(1);
        var bar = new BarPlugin(0);
        var baz = new BazPlugin(-1);

        var config = parser.addPlugin(foo).addPlugin(bar).addPlugin(baz)
                .getConfig();

        assertEquals(List.of(baz, bar, foo),
                new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_AllowAdjustingOpenAPI() {
        Consumer<OpenAPI> adjuster = openAPI -> openAPI.getInfo()
                .setTitle("My Application");
        var config = parser.adjustOpenAPI(adjuster).getConfig();
        adjuster.accept(defaultOpenAPI);

        assertEquals(defaultOpenAPI, config.getOpenAPI());
    }

    @Test
    public void should_AllowPreservingAlreadySetProperties() {
        var config = parser.classPath(List.of("somepath"), false)
                .endpointAnnotation("com.example.Endpoint", false).getConfig();

        assertEquals(defaultClassPathElements, config.getClassPathElements());
    }

    @Test
    public void should_CreateConfigWithDefaultParameters() {
        var config = parser.getConfig();

        assertEquals(defaultClassPathElements, config.getClassPathElements());
        assertEquals(defaultEndpointAnnotationName,
                config.getEndpointAnnotationName());
        assertEquals(defaultEndpointExposedAnnotationName,
                config.getEndpointExposedAnnotationName());
        assertEquals(defaultOpenAPI, config.getOpenAPI());
        assertEquals(List.of(), new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_ParseJSONOpenAPISource()
            throws URISyntaxException, IOException {
        testOpenAPISourceFile("openapi-base.json", OpenAPIFileType.JSON);
    }

    @Test
    public void should_ParseYAMLOpenAPISource()
            throws URISyntaxException, IOException {
        testOpenAPISourceFile("openapi-base.yml", OpenAPIFileType.YAML);
    }

    @Test
    public void should_ThrowError_When_ClassPathIsNotSet() {
        assertThrows(NullPointerException.class, () -> new Parser().execute(),
                "[JVM Parser] classPath is not provided.");
    }

    @Test
    public void should_ThrowError_When_EndpointAnnotationNameIsNotSet() {
        assertThrows(NullPointerException.class,
                () -> new Parser().classPath(defaultClassPathElements)
                        .execute(),
                "[JVM Parser] endpointAnnotationName is not provided.");
    }

    @Test
    public void should_ThrowError_When_UsingWrongPluginConfigInstance() {
        assertThrows(IllegalArgumentException.class, () -> new BazPlugin(0)
                .setConfiguration(new PluginConfiguration() {
                }), "Requires instance of " + BazPluginConfig.class.getName());
    }

    private void testOpenAPISourceFile(String fileName, OpenAPIFileType type)
            throws URISyntaxException, IOException {
        var openAPISource = resourceLoader.readToString(fileName);
        var config = parser.openAPISource(openAPISource, type).getConfig();
        var expected = type.getMapper().readValue(openAPISource, OpenAPI.class);

        assertEquals(expected, config.getOpenAPI());
    }

    private static class BarPlugin extends AbstractPlugin<PluginConfiguration> {
        BarPlugin(int order) {
            setOrder(order);
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }

    private static class BazPlugin extends AbstractPlugin<BazPluginConfig> {
        BazPlugin(int order) {
            setOrder(order);
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }

    private static class BazPluginConfig implements PluginConfiguration {
    }

    private static class FooPlugin extends AbstractPlugin<PluginConfiguration> {
        FooPlugin(int order) {
            setOrder(order);
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }
}
