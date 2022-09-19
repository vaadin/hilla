package dev.hilla.parser.core;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dev.hilla.parser.testutils.ResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PluginOrderTests {
    private ParserConfig.Builder defaultBuilder;
    private final List<String> footsteps = new LinkedList<>();

    @BeforeEach
    public void setup() throws URISyntaxException {
        var resourceLoader = new ResourceLoader(getClass());
        var targetDir = resourceLoader.findTargetDirPath();
        defaultBuilder = new ParserConfig.Builder()
                .classPath(Set.of(targetDir.toString()))
                .endpointAnnotation("dev.hilla.Endpoint");
    }

    @Test
    public void should_AllowPluginsInCorrectOrder() {
        var plugins = List.of(
            new FirstPlugin(), new MiddlePlugin(), new LastPlugin()
        );
        var config = defaultBuilder.plugins(plugins).finish();

        new Parser(config).execute();

        var expectedSteps = new LinkedList<String>();
        expectedSteps.add("FirstPlugin scan");
        expectedSteps.add("MiddlePlugin scan");
        expectedSteps.add("LastPlugin scan");
        expectedSteps.add("FirstPlugin enter");
        expectedSteps.add("MiddlePlugin enter");
        expectedSteps.add("LastPlugin enter");
        expectedSteps.add("LastPlugin exit");
        expectedSteps.add("MiddlePlugin exit");
        expectedSteps.add("FirstPlugin exit");
        assertEquals(expectedSteps, footsteps);
    }

    private class TestBasePlugin extends AbstractPlugin<PluginConfiguration> {
        @Nonnull
        @Override
        public NodeDependencies scan(
            @Nonnull NodeDependencies nodeDependencies) {
            addFootstep("scan");
            return nodeDependencies;
        }

        @Override
        public void enter(NodePath<?> nodePath) {
            addFootstep("enter");
        }

        @Override
        public void exit(NodePath<?> nodePath) {
            addFootstep("exit");
        }

        private void addFootstep(@Nonnull String name) {
            footsteps.add(getClass().getSimpleName() + " " + name);
        }
    }

    private class FirstPlugin extends TestBasePlugin {
        @Override
        public Collection<Class<? extends Plugin>> runBefore() {
            return Set.of(LastPlugin.class);
        }
    }

    private class LastPlugin extends TestBasePlugin {
        @Override
        public Collection<Class<? extends Plugin>> runAfter() {
            return Set.of(FirstPlugin.class);
        }
    }

    private class MiddlePlugin extends TestBasePlugin {
        @Override
        public Collection<Class<? extends Plugin>> runAfter() {
            return Set.of(FirstPlugin.class);
        }

        @Override
        public Collection<Class<? extends Plugin>> runBefore() {
            return Set.of(LastPlugin.class);
        }
    }

    private class UnspecifiedOrderPlugin extends TestBasePlugin {
    }
}
