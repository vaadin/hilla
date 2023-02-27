package dev.hilla.internal;

import java.io.File;
import java.net.URI;

// FIXME: remove after https://github.com/vaadin/flow/issues/16005 is done
final class FlowOptions {
    private final boolean enablePnpm;
    private final boolean nodeAutoUpdate;
    private final URI nodeDownloadRoot;
    private final String nodeVersion;
    private final com.vaadin.flow.server.frontend.Options options;
    private final boolean productionMode;
    private final boolean requireHomeNodeExec;
    private final boolean useGlobalPnpm;

    FlowOptions(com.vaadin.flow.server.frontend.Options options) {
        try {
            this.options = options;
            this.enablePnpm = (Boolean) unwrapField(options, "enablePnpm");
            this.nodeAutoUpdate = (Boolean) unwrapField(options,
                    "nodeAutoUpdate");
            this.nodeDownloadRoot = (URI) unwrapField(options,
                    "nodeDownloadRoot");
            this.nodeVersion = (String) unwrapField(options, "nodeVersion");
            this.productionMode = (Boolean) unwrapField(options,
                    "productionMode");
            this.requireHomeNodeExec = (Boolean) unwrapField(options,
                    "requireHomeNodeExec");
            this.useGlobalPnpm = (Boolean) unwrapField(options,
                    "useGlobalPnpm");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new EngineRuntimeException("Cannot unwrap options", e);
        }
    }

    private static Object unwrapField(
            com.vaadin.flow.server.frontend.Options options, String name)
            throws NoSuchFieldException, IllegalAccessException {
        var field = com.vaadin.flow.server.frontend.Options.class
                .getDeclaredField(name);
        field.setAccessible(true);
        return field.get(options);
    }

    public URI getNodeDownloadRoot() {
        return nodeDownloadRoot;
    }

    public String getNodeVersion() {
        return nodeVersion;
    }

    public File getNpmFolder() {
        return options.getNpmFolder();
    }

    public boolean isDevBundleBuild() {
        return options.isDevBundleBuild();
    }

    public boolean isFrontendHotdeploy() {
        return options.isFrontendHotdeploy();
    }

    public boolean isNodeAutoUpdate() {
        return nodeAutoUpdate;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public boolean isRequireHomeNodeExec() {
        return requireHomeNodeExec;
    }

    public boolean shouldEnablePnpm() {
        return enablePnpm;
    }

    public boolean shouldUseGlobalPnpm() {
        return useGlobalPnpm;
    }
}
