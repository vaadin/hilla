package dev.hilla.parser.test.helpers;

import java.net.URISyntaxException;
import java.nio.file.Path;

import io.github.classgraph.ClassGraph;

import dev.hilla.parser.testutils.ResourceLoader;

public final class TestHelper {
    private final ResourceLoader resourceLoader = createResourceLoader(
            getClass());
    private final Path targetDir;

    {
        try {
            targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassGraph createClassGraph() {
        return new ClassGraph().enableAllInfo()
                .overrideClasspath(targetDir.toString());
    }

    public static ResourceLoader createResourceLoader(Class<?> target) {
        return new ResourceLoader(target::getResource,
                target::getProtectionDomain);
    }
}
