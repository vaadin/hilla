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

import dev.hilla.engine.ParserProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.EndpointGeneratorTaskFactory;
import com.vaadin.flow.server.frontend.Options;
import com.vaadin.flow.server.frontend.TaskGenerateEndpoint;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

/**
 * An implementation of the EndpointGeneratorTaskFactory, which creates endpoint
 * generator tasks.
 */
public class EndpointGeneratorTaskFactoryImpl
        implements EndpointGeneratorTaskFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserProcessor.class);

    @Override
    public TaskGenerateEndpoint createTaskGenerateEndpoint(Options options) {
        if (!options.isDevBundleBuild() && !options.isFrontendHotdeploy()
                && !isProductionMode(options)) {
            // Skip for prepare-frontend phase and in production server
            return new SkipTaskGenerateEndpoint();
        }

        return new TaskGenerateEndpointImpl(options.getNpmFolder(),
                options.getBuildDirectoryName(),
                options.getFrontendGeneratedFolder());
    }

    @Override
    public TaskGenerateOpenAPI createTaskGenerateOpenAPI(Options options) {
        if (!options.isDevBundleBuild() && !options.isFrontendHotdeploy()
                && !isProductionMode(options)) {
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
        public void execute() throws ExecutionFailedException {
            logger.debug("Skipping generating TypeScript endpoints");
        }
    }

    private static class SkipTaskGenerateOpenAPI
            implements TaskGenerateOpenAPI {
        @Override
        public void execute() throws ExecutionFailedException {
            logger.debug("Skipping generating OpenAPI spec");
        }
    }

    // FIXME: remove after https://github.com/vaadin/flow/issues/16005 is done
    private boolean isProductionMode(Options options) {
        try {
            var field = Options.class.getDeclaredField("productionMode");
            field.setAccessible(true);
            return (Boolean) field.get(options);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return false;
        }
    }
}
