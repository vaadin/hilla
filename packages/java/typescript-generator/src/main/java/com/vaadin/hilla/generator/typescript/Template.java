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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills the holes of a piece of TypeScript written as a text block.
 *
 * <p>
 * A hole is named, as in <code>{{name}}</code>, so that a template reads as the
 * TypeScript it produces and the value of each hole is named where it is
 * passed. Positional formatting reads the other way round: the shape is there
 * but which value goes where is not.
 *
 * <p>
 * The braces are doubled because a single pair is ordinary TypeScript, in an
 * object literal for one, and a template literal of TypeScript is written
 * <code>${...}</code>, which leaves <code>{{...}}</code> free.
 */
final class Template {
    private static final Pattern HOLE = Pattern.compile("\\{\\{(\\w+)}}");

    private final String template;
    private final Map<String, String> values = new HashMap<>();

    private Template(String template) {
        this.template = template;
    }

    /**
     * A template to fill, which is written as a text block of the TypeScript it
     * produces.
     */
    static Template of(String template) {
        return new Template(template);
    }

    /**
     * The value of one hole of the template, named as the hole is.
     */
    Template with(String name, String value) {
        values.put(name, value);
        return this;
    }

    /**
     * @throws IllegalArgumentException
     *             if the template has a hole which was given no value, which is
     *             a typo in one of the two
     */
    String fill() {
        var result = new StringBuilder();
        var matcher = HOLE.matcher(template);

        while (matcher.find()) {
            var name = matcher.group(1);
            var value = values.get(name);

            if (value == null) {
                throw new IllegalArgumentException(
                        "Nothing to fill {{" + name + "}} with");
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }

        return matcher.appendTail(result).toString();
    }
}
