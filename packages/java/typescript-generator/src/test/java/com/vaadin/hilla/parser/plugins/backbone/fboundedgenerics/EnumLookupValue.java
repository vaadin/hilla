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
 * An entity parameterized by an enum. The bound of its type parameter is
 * F-bounded, which is how {@link Enum} itself is declared and therefore the
 * only way to constrain a type parameter to be an enum.
 */
public class EnumLookupValue<E extends Enum<E>> {
    private E key;
    private String label;

    public E getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }
}
