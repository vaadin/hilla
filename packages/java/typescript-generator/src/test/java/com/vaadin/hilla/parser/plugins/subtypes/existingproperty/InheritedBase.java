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
package com.vaadin.hilla.parser.plugins.subtypes.existingproperty;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A hierarchy whose discriminator is declared by the base class alone, which is
 * how {@code As.EXISTING_PROPERTY} is usually written: the subtypes inherit the
 * property rather than declaring it again, so their own schemas do not mention
 * it and each of them gets a discriminator of its own instead.
 *
 * <p>
 * The model of the base keeps the property, unlike the models of the subtypes,
 * which leaves a form for a subtype with a field it can only set to the one
 * value the subtype accepts. Leaving out a property of the very type which
 * declares it would be worse, so the models follow where the property is
 * declared.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "flavour")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InheritedBase.Sweet.class, name = "sweet"),
        @JsonSubTypes.Type(value = InheritedBase.Sour.class, name = "sour") })
public class InheritedBase {
    public String flavour;

    public String label;

    public static class Sweet extends InheritedBase {
        public String note;
    }

    public static class Sour extends InheritedBase {
        public int level;
    }
}
