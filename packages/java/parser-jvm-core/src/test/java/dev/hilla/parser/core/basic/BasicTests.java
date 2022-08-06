package dev.hilla.parser.core.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.hilla.parser.core.Parser;
import dev.hilla.parser.core.ParserConfig;
import dev.hilla.parser.testutils.ResourceLoader;

public class BasicTests {
    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_UpdateNodesAndCollectNames() {
        var config = new ParserConfig.Builder().classPath(classPath)
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BasicPlugin()).finish();

        var parser = new Parser(config);

        parser.execute();

        assertEquals(
                List.of("FieldInfoModel foo", "MethodInfoModel methodFoo",
                        "MethodInfoModel methodBar", "FieldInfoModel fieldFoo",
                        "FieldInfoModel fieldBar"),
                parser.getStorage().getPluginStorage()
                        .get(BasicPlugin.STORAGE_KEY));
    }
}
