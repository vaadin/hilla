/*
 * Copyright 2000-2024 Vaadin Ltd.
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

package com.vaadin.hilla.signals.core.event;

/**
 * An exception thrown when the event type is null or invalid.
 */
public class InvalidEventTypeException extends RuntimeException {
    public InvalidEventTypeException(String message) {
        super(message);
    }

    public InvalidEventTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
