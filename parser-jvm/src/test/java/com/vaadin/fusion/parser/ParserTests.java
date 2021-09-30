package com.vaadin.fusion.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.fusion.parser.basic.BasicPlugin;

public class ParserTests {
    private Path targetDirPath;

    @BeforeEach
    public void setup() throws URISyntaxException {
        targetDirPath = Paths.get(
                Objects.requireNonNull(getClass().getResource("/")).toURI())
                .getParent();
    }

    @Test
    public void should_RunBasicPlugin() {
        Parser parser = new Parser().classPath(targetDirPath.toString())
                .endpointAnnotationName(Endpoint.class.getName())
                .pluginClassNames(BasicPlugin.class.getName());

        parser.execute();

        List<String> expected = Arrays.asList("foo", "bar", "getFoo", "baz",
                "getBar", "com.vaadin.fusion.parser.basic.BasicEndpoint$Bar",
                "com.vaadin.fusion.parser.basic.BasicEndpoint$Foo");
        List<String> actual = (List<String>) parser.getStorage()
                .getPluginStorage().get(BasicPlugin.STORAGE_KEY);

        assertEquals(expected, actual);
    }
}
