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

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

import com.vaadin.flow.server.frontend.FallibleCommand;
import com.vaadin.hilla.engine.EngineConfiguration;

/**
 * Abstract class for endpoint related generators.
 */
abstract class AbstractTaskEndpointGenerator implements FallibleCommand {
    private static boolean firstRun = true;

    private EngineConfiguration engineConfiguration;

    AbstractTaskEndpointGenerator(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = Objects.requireNonNull(engineConfiguration,
                "Engine configuration cannot be null");
    }

    protected EngineConfiguration getEngineConfiguration() {
        return engineConfiguration;
    }
}
