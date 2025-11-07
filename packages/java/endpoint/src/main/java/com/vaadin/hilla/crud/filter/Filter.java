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
package com.vaadin.hilla.crud.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Superclass for all filters to be used with CRUD services. This specific class
 * is never used, instead a filter instance will be one of the following types:
 * <ul>
 * <li>{@link AndFilter} - Contains a list of nested filters, all of which need
 * to pass.</li>
 * <li>{@link OrFilter} - Contains a list of nested filters, of which at least
 * one needs to pass.</li>
 * <li>{@link PropertyStringFilter} - Matches a specific property, or nested
 * property path, against a filter value, using a specific operator.</li>
 * </ul>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @Type(value = OrFilter.class, name = "or"),
        @Type(value = AndFilter.class, name = "and"),
        @Type(value = PropertyStringFilter.class, name = "propertyString") })
public class Filter {

}
