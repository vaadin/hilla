package dev.hilla.parser.core.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.core.dependency.DependencyPlugin;
import dev.hilla.parser.core.hierarchy.HierarchyPlugin;
import dev.hilla.parser.testutils.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicTests {
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private Set<String> defaultClassPathElements;
    private Path targetDir;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDir = resourceLoader.findTargetDirPath();
        defaultClassPathElements = Set.of(targetDir.toString());
    }

    @Test
    public void should_RunBasicPlugin() {
        var parser = new Parser(new ParserConfig.Builder()
                .classPath(defaultClassPathElements)
                .endpointAnnotation(Endpoint.class.getName())
                .endpointExposedAnnotation(EndpointExposed.class.getName())
                .addPlugin(new BasicPlugin()).finish());

        parser.execute();

        var expected = List.of("foo", "bar", "getFoo", "baz", "getBar",
                "dev.hilla.parser.core.basic.BasicEndpoint$Bar",
                "dev.hilla.parser.core.basic.BasicEndpoint$Foo");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
