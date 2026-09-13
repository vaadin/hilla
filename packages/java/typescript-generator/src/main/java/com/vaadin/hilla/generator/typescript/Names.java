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

import java.util.Set;
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

    /**
     * The words TypeScript does not accept as the name of a declaration. Only
     * those which a Java or Kotlin method can also be called are of interest,
     * but the rest cost nothing and keep the list the list of the language
     * rather than a selection someone has to keep up to date.
     */
    private static final Set<String> RESERVED = Set.of("await", "break", "case",
            "catch", "class", "const", "continue", "debugger", "default",
            "delete", "do", "else", "enum", "export", "extends", "false",
            "finally", "for", "function", "if", "implements", "import", "in",
            "instanceof", "interface", "let", "new", "null", "package",
            "private", "protected", "public", "return", "static", "super",
            "switch", "this", "throw", "true", "try", "typeof", "var", "void",
            "while", "with", "yield");

    private Names() {
    }

    /**
     * Whether TypeScript reads the name as a word of the language, and a
     * declaration going by it therefore has to be named something else.
     */
    static boolean isReserved(String name) {
        return RESERVED.contains(name);
    }

    /**
     * The name of a property as it is written in a type or an object, quoted
     * when it is not a name TypeScript accepts as it is.
     */
    static String property(String name) {
        return IDENTIFIER.matcher(name).matches() ? name : "'" + name + "'";
    }
}
