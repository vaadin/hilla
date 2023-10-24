/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package dev.hilla.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendToolsSettings;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import dev.hilla.engine.ParserProcessor;

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
        if (!options.isDevBundleBuild() && !options.isFrontendHotdeploy()
                && !options.isProductionMode()) {
            // Skip for prepare-frontend phase and in production server
            return new SkipTaskGenerateEndpoint();
        }

        var nodeExecutable = buildTools(options).getNodeExecutable();

        return new TaskGenerateEndpointImpl(options.getNpmFolder(),
                options.getBuildDirectoryName(),
                options.getFrontendGeneratedFolder(),
                options.isProductionMode(), nodeExecutable);
    }

    @Override
    public TaskGenerateOpenAPI createTaskGenerateOpenAPI(Options options) {
        if (!options.isDevBundleBuild() && !options.isFrontendHotdeploy()
                && !options.isProductionMode()) {
            // Skip for prepare-frontend phase and in production server
            return new SkipTaskGenerateOpenAPI();
        }

        return new TaskGenerateOpenAPIImpl(options.getNpmFolder(),
                options.getBuildDirectoryName(),
                options.getFrontendGeneratedFolder(),
                options.getClassFinder().getClassLoader());
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
