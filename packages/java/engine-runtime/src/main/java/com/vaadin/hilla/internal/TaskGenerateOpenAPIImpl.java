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
package com.vaadin.hilla.internal;

import com.vaadin.hilla.engine.EngineAutoConfiguration;
import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateOpenAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of TaskGenerateOpenAPI.
 *
 * OpenAPI generation has been removed from Hilla. TypeScript is now generated
 * directly from Java classes without using OpenAPI as an intermediate format.
 *
 * This class is kept as a no-op to satisfy the TaskGenerateOpenAPI interface
 * requirement, but does nothing.
 */
public class TaskGenerateOpenAPIImpl extends AbstractTaskEndpointGenerator
        implements TaskGenerateOpenAPI {
    private static final Logger logger = LoggerFactory
            .getLogger(TaskGenerateOpenAPIImpl.class);

    /**
     * Create a task for generating OpenAPI spec.
     *
     * @param engineConfiguration
     *            Hilla engine configuration instance
     */
    TaskGenerateOpenAPIImpl(EngineAutoConfiguration engineConfiguration) {
        super(engineConfiguration);
    }

    /**
     * No-op implementation. OpenAPI generation has been removed.
     *
     * @throws ExecutionFailedException
     *             never thrown
     */
    @Override
    public void execute() throws ExecutionFailedException {
        logger.debug(
                "OpenAPI generation skipped - Hilla no longer generates OpenAPI specs. "
                        + "TypeScript is generated directly from Java classes.");
        // No-op: OpenAPI generation has been removed from Hilla
    }
}
