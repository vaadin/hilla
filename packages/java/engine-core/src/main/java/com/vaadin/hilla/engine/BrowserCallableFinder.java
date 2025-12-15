/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.engine;

import java.util.List;

/**
 * Functional interface for finding browser-callable classes. Implementations of
 * this interface are responsible for locating and returning a list of endpoint
 * classes, or throwing an exception if the search cannot be completed due to an
 * error.
 */
@FunctionalInterface
public interface BrowserCallableFinder {
    /**
     * Finds and returns a list of browser-callable classes based on the
     * provided configuration.
     *
     * @param engineConfiguration
     *            The configuration to use for finding classes.
     * @return A list of classes that are browser-callable.
     * @throws BrowserCallableFinderException
     *             If an error occurs while finding the classes.
     */
    List<Class<?>> find(EngineAutoConfiguration engineConfiguration)
            throws BrowserCallableFinderException;
}
