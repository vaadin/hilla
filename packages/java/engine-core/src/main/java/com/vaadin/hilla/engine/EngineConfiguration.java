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

/**
 * Configuration for the generator engine. It exposes all properties that can be
 * overridden by an alternative implementation. All methods take the default
 * value as a parameter and return the default value if not overridden.
 */
public interface EngineConfiguration {
    /**
     * Returns the finder for browser-callable classes. This is used when
     * building the application for production.
     *
     * @param defaultFinder
     *            The default finder to use if no custom finder is provided.
     * @return The browser-callable finder to use.
     */
    default BrowserCallableFinder getBrowserCallableFinder(
            BrowserCallableFinder defaultFinder) {
        return defaultFinder;
    }

    /**
     * Returns the class loader to use for loading classes. This is used when
     * building the application for production.
     *
     * @param defaultClassLoader
     *            the default class loader
     * @return the class loader to use
     */
    default ClassLoader getClassLoader(ClassLoader defaultClassLoader) {
        return defaultClassLoader;
    }
}
