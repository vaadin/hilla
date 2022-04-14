package dev.hilla.parser.test.helpers;

import java.net.URISyntaxException;
import java.nio.file.Path;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import dev.hilla.parser.testutils.ResourceLoader;

public final class SourceHelper {
    private final ResourceLoader resourceLoader = createResourceLoader(
            getClass());
    private final Path targetDir;
    private ScanResult scanResult;

    {
        try {
            targetDir = resourceLoader.findTargetDirPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void init() {
        scanResult = new ClassGraph().enableAllInfo()
            .overrideClasspath(targetDir.toString()).scan();
    }

    public void fin() {
        scanResult.close();
    }

    public static ResourceLoader createResourceLoader(Class<?> target) {
        return new ResourceLoader(target::getResource,
                target::getProtectionDomain);
    }
}
