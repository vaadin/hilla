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
package com.vaadin.hilla.maven;

import org.apache.maven.plugin.MojoFailureException;

/**
 * Exception thrown when the engine generation fails.
 */
public class EngineGenerateMojoException extends MojoFailureException {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            Message for the exception.
     */
    public EngineGenerateMojoException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            Message for the exception.
     * @param cause
     *            Cause of the exception.
     */
    public EngineGenerateMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
