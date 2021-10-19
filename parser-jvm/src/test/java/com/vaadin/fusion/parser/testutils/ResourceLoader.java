package com.vaadin.fusion.parser.testutils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class ResourceLoader {
    private final Class<?> targetClass;

    public ResourceLoader(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Path findTargetDirPath() throws URISyntaxException {
        return Paths.get(
                Objects.requireNonNull(targetClass.getResource("/")).toURI())
                .getParent();
    }

    public File find(String resourceName) throws URISyntaxException {
        return Paths.get(
                Objects.requireNonNull(targetClass.getResource(resourceName)).toURI())
                .toFile();
    }
}
