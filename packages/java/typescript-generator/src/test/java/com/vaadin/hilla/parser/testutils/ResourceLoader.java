package com.vaadin.hilla.parser.testutils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public final class ResourceLoader {
    private final Class<?> cls;

    public ResourceLoader(Class<?> cls) {
        this.cls = cls;
    }

    public static String getClasspath(ResourceLoader... loaders)
            throws URISyntaxException {
        return getClasspath(Arrays.asList(loaders));
    }

    public static String getClasspath(Collection<ResourceLoader> loaders)
            throws URISyntaxException {
        var builder = new StringBuilder(System.getProperty("java.class.path"));

        for (var loader : loaders) {
            var path = loader.findTargetPath().toString();
            builder.append(File.pathSeparatorChar);
            builder.append(path);
        }

        return builder.toString();
    }

    public File find(String resourceName) throws URISyntaxException {
        return Paths.get(
                Objects.requireNonNull(cls.getResource(resourceName)).toURI())
                .toFile();
    }

    public Path findTargetDirPath() throws URISyntaxException {
        return findTargetPath().getParent();
    }

    public Path findTargetPath() throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(
                        cls.getProtectionDomain().getCodeSource().getLocation())
                .toURI());
    }

    public String readToString(String resourceName)
            throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(find(resourceName).toPath()));
    }
}
