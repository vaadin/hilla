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
package com.vaadin.hilla.parser.plugins.subtypes;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An intermediate class that is not mentioned in {@code @JsonSubTypes} and
 * whose name loses to the one of an interface, as it does in Jackson.
 */
@JsonTypeName("recurring")
public abstract class RecurringJob extends Job {

    public int intervalMinutes;
}
