/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.fusion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A checker responsible for validating the Vaadin endpoint names.
 */
public class EndpointNameChecker {
    /**
     * Set of reserved words in ECMAScript specification. Also covers all the
     * reserved identifiers in TypeScript.
     *
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#Keywords">JavaScript
     *      Reference on MDN</a>
     * @see <a href=
     *      "https://github.com/Microsoft/TypeScript/blob/master/doc/TypeScript%20Language%20Specification.pdf">TypeScript
     *      Language Specification</a>
     */
    public static final Set<String> ECMA_SCRIPT_RESERVED_WORDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("abstract",
                    "arguments", "await", "boolean", "break", "byte", "case",
                    "catch", "char", "class", "const", "continue", "debugger",
                    "default", "delete", "do", "double", "else", "enum", "eval",
                    "export", "extends", "false", "final", "finally", "float",
                    "for", "function", "goto", "if", "implements", "import",
                    "in", "instanceof", "int", "interface", "let", "long",
                    "native", "new", "null", "package", "private", "protected",
                    "public", "return", "short", "static", "super", "switch",
                    "synchronized", "this", "throw", "throws", "transient",
                    "true", "try", "typeof", "var", "void", "volatile", "while",
                    "with", "yield")));

    private static final Pattern CONTAINS_WHITESPACE_PATTERN = Pattern
            .compile(".*[\\s+].*");

    /**
     * Validates the Vaadin endpoint name given.
     *
     * @param endpointName
     *            the name to validate
     * @return {@code null} if there are no validation errors or {@link String}
     *         containing validation error description.
     */
    public String check(String endpointName) {
        if (endpointName == null || endpointName.isEmpty()) {
            return "Endpoint name cannot be blank";
        }
        if (ECMA_SCRIPT_RESERVED_WORDS.contains(endpointName)) {
            return "Endpoint name cannot be equal to JavaScript reserved words";
        }
        if (CONTAINS_WHITESPACE_PATTERN.matcher(endpointName).matches()) {
            return "Endpoint name cannot contain any whitespaces";
        }
        return null;
    }
}
