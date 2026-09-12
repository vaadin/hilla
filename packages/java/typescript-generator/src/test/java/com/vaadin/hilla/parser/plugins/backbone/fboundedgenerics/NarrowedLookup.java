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
package com.vaadin.hilla.parser.plugins.backbone.fboundedgenerics;

/**
 * An entity whose type parameter has a bound leading back to it next to one
 * which does not: only the first one is left out, so the type parameter stays
 * bounded by {@link EnumLookupValue}.
 */
public class NarrowedLookup<T extends EnumLookupValue<NodeType> & Comparable<T>> {
    private T value;

    public T getValue() {
        return value;
    }
}
