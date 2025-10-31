package com.vaadin.hilla.parser.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.testutils.ResourceLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

public class ParserConfigTests {
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BrowserCallable {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Endpoint {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EndpointExposed {
    }

    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Set<String> defaultClassPathElements;
    private List<Class<? extends Annotation>> defaultEndpointAnnotations;
    private List<Class<? extends Annotation>> defaultEndpointExposedAnnotations;
    private OpenAPI defaultOpenAPI;
    private Parser parser;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        defaultEndpointAnnotations = List.of(BrowserCallable.class,
                Endpoint.class);

        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
        defaultEndpointExposedAnnotations = List.of(EndpointExposed.class);
        defaultOpenAPI = new OpenAPI()
                .info(new Info().title("Hilla Application").version("1.0.0"))
                .servers(List
                        .of(new Server().url("http://localhost:8080/connect")
                                .description("Hilla Backend")))
                .paths(new Paths());
        parser = new Parser().classPath(defaultClassPathElements)
                .endpointAnnotations(defaultEndpointAnnotations)
                .endpointExposedAnnotations(defaultEndpointExposedAnnotations);
    }

    @Test
    public void should_AllowAddingPluginsAsCollection() {
        var foo = new FooPlugin();
        var bar = new BarPlugin();
        var baz = new BazPlugin();

        var config = parser.plugins(List.of(foo, bar, baz)).getConfig();

        assertEquals(List.of(foo, bar, baz),
                new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_AllowAddingPluginsAsVararg() {
        var foo = new FooPlugin();
        var bar = new BarPlugin();
        var baz = new BazPlugin();

        var config = parser.plugins(foo, bar, baz).getConfig();

        assertEquals(List.of(foo, bar, baz),
                new ArrayList<>(config.getPlugins()));
    }

    @Test
    public void should_AllowAddingPluginsOneByOne() {
        var foo = new FooPlugin();
        var bar = new BarPlugin();
        var baz = new BazPlugin();

        var config = parser.addPlugin(foo).addPlugin(bar).addPlugin(baz)
                .getConfig();

        assertEquals(List.of(foo, bar, baz),
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
                .endpointAnnotations(List.of(Endpoint.class), false)
                .getConfig();

        assertEquals(defaultClassPathElements, config.getClassPathElements());
    }

    @Test
    public void should_CreateConfigWithDefaultParameters() {
        var config = parser.getConfig();

        assertEquals(defaultClassPathElements, config.getClassPathElements());
        assertEquals(defaultEndpointAnnotations,
                config.getEndpointAnnotations());
        assertEquals(defaultEndpointExposedAnnotations,
                config.getEndpointExposedAnnotations());
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
        var e = assertThrows(NullPointerException.class,
                () -> new Parser()
                        .endpointAnnotations(defaultEndpointAnnotations)
                        .execute(List.of()));
        assertEquals("[JVM Parser] classPath is not provided.", e.getMessage());
    }

    @Test
    public void should_ThrowError_When_EndpointAnnotationsIsNotSet() {
        var e = assertThrows(IllegalArgumentException.class, () -> new Parser()
                .classPath(defaultClassPathElements).execute(List.of()));
        assertEquals("[JVM Parser] endpoint annotations are not provided.",
                e.getMessage());
    }

    @Test
    public void should_ThrowError_When_UsingWrongPluginConfigInstance() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> new BazPlugin()
                        .setConfiguration(new PluginConfiguration() {
                        }));
        assertThat(e.getMessage(), startsWith("Requires instance of class "
                + BazPluginConfig.class.getName()));
    }

    private void testOpenAPISourceFile(String fileName, OpenAPIFileType type)
            throws URISyntaxException, IOException {
        var openAPISource = resourceLoader.readToString(fileName);
        var config = parser.openAPISource(openAPISource, type).getConfig();
        var expected = type.getMapper().readValue(openAPISource, OpenAPI.class);

        assertEquals(expected, config.getOpenAPI());
    }

    private static class BarPlugin extends AbstractPlugin<PluginConfiguration> {
        BarPlugin() {
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @NonNull
        @Override
        public NodeDependencies scan(
                @NonNull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }

    private static class BazPlugin extends AbstractPlugin<BazPluginConfig> {
        BazPlugin() {
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @NonNull
        @Override
        public NodeDependencies scan(
                @NonNull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }

    private static class BazPluginConfig implements PluginConfiguration {
    }

    private static class FooPlugin extends AbstractPlugin<PluginConfiguration> {
        FooPlugin() {
        }

        @Override
        public void enter(NodePath<?> nodePath) {
        }

        @Override
        public void exit(NodePath<?> nodePath) {
        }

        @NonNull
        @Override
        public NodeDependencies scan(
                @NonNull NodeDependencies nodeDependencies) {
            return nodeDependencies;
        }
    }
}
