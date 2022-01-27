package dev.hilla.parser.testutils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
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

    public File find(String resourceName) throws URISyntaxException {
        return Paths.get(
                Objects.requireNonNull(getResource.apply(resourceName)).toURI())
                .toFile();
    }

    public Path findTargetDirPath() throws URISyntaxException {
        return Paths.get(Objects
                .requireNonNull(
                        getProtectionDomain.get().getCodeSource().getLocation())
                .toURI()).getParent();
    }

    public String readToString(String resourceName)
            throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(find(resourceName).toPath()));
    }
}
