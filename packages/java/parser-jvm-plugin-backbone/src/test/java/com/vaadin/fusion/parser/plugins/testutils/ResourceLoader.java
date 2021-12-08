package com.vaadin.fusion.parser.plugins.testutils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class ResourceLoader {
    private final Class<?> targetClass;

    public ResourceLoader(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public File find(String resourceName) throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(targetClass.getResource(resourceName)).toURI())
                .toFile();
    }

    public Path findTargetDirPath() throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                targetClass.getProtectionDomain().getCodeSource().getLocation())
                .toURI()).getParent();
    }

    public String readToString(String resourceName)
            throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(find(resourceName).toPath()));
    }
}
