package dev.hilla.parser.core.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.testutils.ResourceLoader;

public class DependencyTests {
    private static final List<String> classPath;
    private static final ResourceLoader resourceLoader = new ResourceLoader(
            DependencyTests.class);
    private static Parser parser;

    static {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void setUp() {
        parser = new Parser(new ParserConfig.Builder().classPath(classPath)
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new DependencyPlugin())
                .finish());
        parser.execute();
    }

    @Test
    public void should_CollectDependencyMembers() {
        var expected = List.of("foo", "bar", "circular", "getFoo", "setBar",
                "foo", "circular",
                "innerClassMember");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.DEPS_MEMBERS_STORAGE_KEY);

        assertEquals(expected, actual);
    }

    @Test
    public void should_ResolveDependencies() {
        var expected = List.of(
                "dev.hilla.parser.core.dependency.DependencyEntityOne",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo",
                "dev.hilla.parser.core.dependency.DependencyEntityTwo$InnerClass");

        var actual = (List<String>) parser.getStorage().getPluginStorage()
                .get(DependencyPlugin.ALL_DEPS_STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
