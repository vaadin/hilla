package dev.hilla.parser.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashSet;
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
    private ParserConfig.Builder defaultBuilder;
    private Set<String> defaultClassPathElements;
    private String defaultEndpointAnnotationName;
    private String defaultEndpointExposedAnnotationName;
    private OpenAPI defaultOpenAPI;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
        defaultEndpointAnnotationName = "dev.hilla.Endpoint";
        defaultEndpointExposedAnnotationName = "dev.hilla.EndpointExposed";
        defaultOpenAPI = new OpenAPI()
                .info(new Info().title("Vaadin Application").version("1.0.0"))
                .servers(List
                        .of(new Server().url("http://localhost:8080/connect")
                                .description("Vaadin Backend")))
                .paths(new Paths());
        defaultBuilder = new ParserConfig.Builder()
                .classPath(defaultClassPathElements)
                .endpointAnnotation(defaultEndpointAnnotationName)
                .endpointExposedAnnotation(
                        defaultEndpointExposedAnnotationName);
    }

    @Test
    public void should_AllowAddingPluginsAsCollection() {
        var plugins = List.of(new FooPlugin(), new BarPlugin());
        var actual = defaultBuilder.plugins(plugins).finish();
        var expected = new TestParserConfig(defaultClassPathElements,
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, defaultOpenAPI,
                new HashSet<>(plugins));

        assertEquals(expected, actual);
    }

    @Test
    public void should_AllowAddingPluginsOneByOne() {
        var fooPlugin = new FooPlugin();
        var barPlugin = new BarPlugin();
        var actual = defaultBuilder.addPlugin(fooPlugin).addPlugin(barPlugin)
                .finish();
        var expected = new TestParserConfig(defaultClassPathElements,
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, defaultOpenAPI,
                Set.of(fooPlugin, barPlugin));

        assertEquals(expected, actual);
    }

    @Test
    public void should_AllowAdjustingOpenAPI() {
        Consumer<OpenAPI> adjuster = openAPI -> openAPI.getInfo()
                .setTitle("My Application");
        var actual = defaultBuilder.adjustOpenAPI(adjuster).finish();

        adjuster.accept(defaultOpenAPI);
        var expected = new TestParserConfig(defaultClassPathElements,
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, defaultOpenAPI, Set.of());

        assertEquals(expected, actual);
    }

    @Test
    public void should_AllowPreservingAlreadySetProperties() {
        var actual = defaultBuilder.classPath(List.of("somepath"), false)
                .endpointAnnotation("com.example.Endpoint", false).finish();
        var expected = new TestParserConfig(defaultClassPathElements,
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, defaultOpenAPI, Set.of());

        assertEquals(expected, actual);
    }

    @Test
    public void should_CreateConfigWithDefaultParameters() {
        var expected = new TestParserConfig(defaultClassPathElements,
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, defaultOpenAPI, Set.of());
        var actual = defaultBuilder.finish();

        assertEquals(expected, actual);
    }

    @Test
    public void should_ParseJSONOpenAPISource()
            throws URISyntaxException, IOException {
        testOpenAPISourceFile("openapi-base.json",
                ParserConfig.OpenAPIFileType.JSON);
    }

    @Test
    public void should_ParseYAMLOpenAPISource()
            throws URISyntaxException, IOException {
        testOpenAPISourceFile("openapi-base.yml",
                ParserConfig.OpenAPIFileType.YAML);
    }

    @Test
    public void should_ThrowError_When_ClassPathIsNotSet() {
        assertThrows(NullPointerException.class,
                () -> new ParserConfig.Builder().finish(),
                "[JVM Parser] classPath is not provided.");
    }

    @Test
    public void should_ThrowError_When_EndpointAnnotationNameIsNotSet() {
        assertThrows(NullPointerException.class,
                () -> new ParserConfig.Builder()
                        .classPath(defaultClassPathElements).finish(),
                "[JVM Parser] endpointAnnotationName is not provided.");
    }

    @Test
    public void should_ThrowError_When_UsingWrongPluginConfigInstance() {
        assertThrows(IllegalArgumentException.class, () -> new BazPlugin()
                .setConfiguration(new PluginConfiguration() {
                }), "Requires instance of " + BazPluginConfig.class.getName());
    }

    private void testOpenAPISourceFile(String fileName,
            ParserConfig.OpenAPIFileType type)
            throws URISyntaxException, IOException {
        var openAPISource = resourceLoader.readToString(fileName);

        var actual = defaultBuilder.openAPISource(openAPISource, type).finish();

        var openAPI = type.getMapper().readValue(openAPISource, OpenAPI.class);
        var expected = new TestParserConfig(Set.of(targetDir.toString()),
                defaultEndpointAnnotationName,
                defaultEndpointExposedAnnotationName, openAPI, Set.of());

        assertEquals(expected, actual);
    }

    private static class BarPlugin extends AbstractPlugin<PluginConfiguration> {
        BarPlugin() {
            super();
            setOrder(1);
        }

        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }
    }

    private static class FooPlugin extends AbstractPlugin<PluginConfiguration> {
        FooPlugin() {
            setOrder(0);
        }

        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }
    }

    private static class BazPluginConfig implements PluginConfiguration {
    }

    private static class BazPlugin extends AbstractPlugin<BazPluginConfig> {
        @Nonnull
        @Override
        public NodeDependencies scan(
                @Nonnull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }
    }

    private static class TestParserConfig extends AbstractParserConfig {
        private final Set<String> classPathElements;
        private final String endpointAnnotationName;
        private final String endpointExposedAnnotationName;
        private final OpenAPI openAPI;
        private final Set<Plugin> plugins;

        public TestParserConfig(Set<String> classPathElements,
                String endpointAnnotationName,
                String endpointExposedAnnotationName, OpenAPI openAPI,
                Set<Plugin> plugins) {
            this.classPathElements = classPathElements;
            this.endpointAnnotationName = endpointAnnotationName;
            this.endpointExposedAnnotationName = endpointExposedAnnotationName;
            this.openAPI = openAPI;
            this.plugins = plugins;
        }

        @Nonnull
        @Override
        public Set<String> getClassPathElements() {
            return classPathElements;
        }

        @Nonnull
        @Override
        public String getEndpointAnnotationName() {
            return endpointAnnotationName;
        }

        @Nonnull
        @Override
        public String getEndpointExposedAnnotationName() {
            return endpointExposedAnnotationName;
        }

        @Nonnull
        @Override
        public OpenAPI getOpenAPI() {
            return openAPI;
        }

        @Nonnull
        @Override
        public Set<Plugin> getPlugins() {
            return plugins;
        }
    }
}
