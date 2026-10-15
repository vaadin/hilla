/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendToolsSettings;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import com.vaadin.hilla.engine.ParserProcessor;

/**
 * An implementation of the EndpointGeneratorTaskFactory, which creates endpoint
 * generator tasks.
 */
public class EndpointGeneratorTaskFactoryImpl
        implements EndpointGeneratorTaskFactory {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ParserProcessor.class);

    private static FrontendTools buildTools(Options options) {
        var settings = new FrontendToolsSettings(
                options.getNpmFolder().getAbsolutePath(),
                () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
        settings.setNodeDownloadRoot(options.getNodeDownloadRoot());
        settings.setForceAlternativeNode(options.isRequireHomeNodeExec());
        settings.setUseGlobalPnpm(options.isUseGlobalPnpm());
        settings.setAutoUpdate(options.isNodeAutoUpdate());
        settings.setNodeVersion(options.getNodeVersion());

        return new FrontendTools(settings);
    }

    @Override
    public TaskGenerateEndpoint createTaskGenerateEndpoint(Options options) {
        if (!options.isRunNpmInstall() && !options.isDevBundleBuild()
                && !options.isProductionMode()) {
            // Skip for prepare-frontend phase and in production server
            return new SkipTaskGenerateEndpoint();
        }

        var nodeExecutable = buildTools(options).getNodeExecutable();

        return new TaskGenerateEndpointImpl(options.getNpmFolder(),
                options.getBuildDirectoryName(),
                options.getFrontendGeneratedFolder(),
                options.getClassFinder()::getResource,
                options.isProductionMode(), nodeExecutable);
    }

    @Override
    public TaskGenerateOpenAPI createTaskGenerateOpenAPI(Options options) {
        if (!options.isRunNpmInstall() && !options.isDevBundleBuild()
                && !options.isProductionMode()) {
            // Skip for prepare-frontend phase and in production server
            return new SkipTaskGenerateOpenAPI();
        }

        return new TaskGenerateOpenAPIImpl(options.getNpmFolder(),
                options.getBuildDirectoryName(),
                options.getFrontendGeneratedFolder(),
                options.getClassFinder()::getResource,
                options.getClassFinder().getClassLoader(),
                options.isProductionMode());
    }

    private static class SkipTaskGenerateEndpoint
            implements TaskGenerateEndpoint {
        @Override
        public void execute() {
            LOGGER.debug("Skipping generating TypeScript endpoints");
        }
    }

    private static class SkipTaskGenerateOpenAPI
            implements TaskGenerateOpenAPI {
        @Override
        public void execute() {
            LOGGER.debug("Skipping generating OpenAPI spec");
        }
    }
}
