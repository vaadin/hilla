package dev.hilla.parser.testutils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ResourceLoader {
    private final Supplier<ProtectionDomain> getProtectionDomain;
    private final Function<String, URL> getResource;

    public ResourceLoader(Function<String, URL> getResource,
            Supplier<ProtectionDomain> getProtectionDomain) {
        this.getProtectionDomain = getProtectionDomain;
        this.getResource = getResource;
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
                Objects.requireNonNull(getResource.apply(resourceName)).toURI())
                .toFile();
    }

    public Path findTargetDirPath() throws URISyntaxException {
        return findTargetPath().getParent();
    }

    public Path findTargetPath() throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(
                        getProtectionDomain.get().getCodeSource().getLocation())
                .toURI());
    }

    public String readToString(String resourceName)
            throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(find(resourceName).toPath()));
    }
}
