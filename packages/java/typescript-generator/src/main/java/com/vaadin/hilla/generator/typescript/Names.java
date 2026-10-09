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
package com.vaadin.hilla.generator.typescript;

import java.util.regex.Pattern;

/**
 * How the names a Java class decides are written in TypeScript.
 */
final class Names {
    /**
     * What TypeScript accepts as a name written as it is. Anything else, such
     * as the {@code @type} Jackson names a discriminator by default, has to be
     * written as a string.
     */
    private static final Pattern IDENTIFIER = Pattern
            .compile("[A-Za-z_$][A-Za-z0-9_$]*");

    private Names() {
    }

    /**
     * The name of a property as it is written in a type or an object, quoted
     * when it is not a name TypeScript accepts as it is.
     */
    static String property(String name) {
        return IDENTIFIER.matcher(name).matches() ? name : "'" + name + "'";
    }
}
