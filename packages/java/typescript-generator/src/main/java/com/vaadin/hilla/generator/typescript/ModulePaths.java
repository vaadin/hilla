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

/**
 * Where the generated files go, and how they refer to each other.
 *
 * <p>
 * A path is always relative to the output folder, with {@code /} as the
 * separator, since it ends up in a TypeScript import rather than on a file
 * system.
 */
final class ModulePaths {
    private ModulePaths() {
    }

    /**
     * The file of an entity, which mirrors the package of the Java class.
     * Nested classes are folders as well, so that a class and its nested
     * classes do not collide.
     */
    static String forEntity(String javaClass, String fromDirectory) {
        return relative(javaClass.replace('.', '/').replace('$', '/'),
                fromDirectory);
    }

    /**
     * The name an entity is generated under, which is the name of the Java
     * class without its package or enclosing classes.
     */
    static String entityName(String javaClass) {
        var separator = javaClass
                .lastIndexOf(javaClass.indexOf('$') >= 0 ? '$' : '.');
        return javaClass.substring(separator + 1);
    }

    /**
     * The file of an endpoint, which sits directly in the output folder.
     */
    static String forEndpoint(String endpointName, String fromDirectory) {
        return relative(endpointName, fromDirectory);
    }

    /**
     * Turns a path relative to the output folder into one relative to the file
     * being written, as a module specifier.
     */
    private static String relative(String path, String fromDirectory) {
        if (fromDirectory.isEmpty()) {
            return "./" + path + ".js";
        }

        var from = fromDirectory.split("/");
        var to = path.split("/");
        var common = 0;

        while (common < from.length && common < to.length - 1
                && from[common].equals(to[common])) {
            common++;
        }

        var builder = new StringBuilder();

        for (var index = common; index < from.length; index++) {
            builder.append("../");
        }

        if (builder.isEmpty()) {
            builder.append("./");
        }

        for (var index = common; index < to.length; index++) {
            builder.append(to[index]);

            if (index < to.length - 1) {
                builder.append('/');
            }
        }

        return builder.append(".js").toString();
    }
}
