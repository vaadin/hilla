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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Collects the imports of one generated file and writes them out.
 *
 * <p>
 * Asking for the same thing twice gives the same local name back, and two
 * different things which would like the same local name get distinct ones, so
 * that the writers can ask for what they need without keeping track of what
 * they asked for before.
 */
final class ImportRegistry {
    private final Map<String, Module> modules = new TreeMap<>(
            ImportRegistry::compareModules);
    private final Set<String> usedNames = new HashSet<>();

    /**
     * Imports the default export of a module, as in
     * {@code import name from 'module'}.
     */
    String importDefault(String module, String preferredName,
            boolean typeOnly) {
        return module(module).defaultImport(preferredName, typeOnly);
    }

    /**
     * Imports a named export of a module, as in {@code import { name } from
     * 'module'}.
     */
    String importNamed(String module, String exportedName, boolean typeOnly) {
        return module(module).namedImport(exportedName, typeOnly);
    }

    /**
     * Imports all the exports of a module, as in
     * {@code import * as name from 'module'}.
     */
    String importAll(String module, String preferredName) {
        return module(module).namespaceImport(preferredName);
    }

    /**
     * Writes the import statements, bare modules first and relative ones after,
     * so that the result only depends on what was imported.
     */
    List<String> write() {
        var lines = new ArrayList<String>();
        modules.forEach((path, module) -> module.write(path, lines));
        return lines;
    }

    boolean isEmpty() {
        return modules.isEmpty();
    }

    private Module module(String module) {
        return modules.computeIfAbsent(module, path -> new Module());
    }

    /**
     * Bare module names sort before relative paths, which is how a hand written
     * file would have them.
     */
    private static int compareModules(String left, String right) {
        var relative = Boolean.compare(isRelative(left), isRelative(right));
        return relative != 0 ? relative : left.compareTo(right);
    }

    private static boolean isRelative(String module) {
        return module.startsWith(".");
    }

    private String uniqueName(String preferredName) {
        var name = preferredName;

        for (var index = 1; !usedNames.add(name); index++) {
            name = preferredName + "_" + index;
        }

        return name;
    }

    private final class Module {
        private final Map<String, String> named = new TreeMap<>();
        private final Set<String> typeOnlyNames = new HashSet<>();
        private String defaultName;
        private boolean defaultTypeOnly;
        private String namespaceName;

        private String defaultImport(String preferredName, boolean typeOnly) {
            if (defaultName == null) {
                defaultName = uniqueName(preferredName);
                defaultTypeOnly = typeOnly;
            } else if (!typeOnly) {
                // A value import covers a type-only one, but not the reverse
                defaultTypeOnly = false;
            }

            return defaultName;
        }

        private String namedImport(String exportedName, boolean typeOnly) {
            var name = named.computeIfAbsent(exportedName,
                    key -> uniqueName(key));

            if (typeOnly) {
                typeOnlyNames.add(exportedName);
            } else {
                typeOnlyNames.remove(exportedName);
            }

            return name;
        }

        private String namespaceImport(String preferredName) {
            if (namespaceName == null) {
                namespaceName = uniqueName(preferredName);
            }

            return namespaceName;
        }

        private void write(String path, List<String> lines) {
            if (namespaceName != null) {
                lines.add("import * as " + namespaceName + " from '" + path
                        + "';");
            }

            if (defaultName != null) {
                lines.add("import " + (defaultTypeOnly ? "type " : "")
                        + defaultName + " from '" + path + "';");
            }

            writeNamed(path, lines, true);
            writeNamed(path, lines, false);
        }

        private void writeNamed(String path, List<String> lines,
                boolean typeOnly) {
            var specifiers = named.entrySet().stream().filter(
                    entry -> typeOnlyNames.contains(entry.getKey()) == typeOnly)
                    .map(entry -> entry.getKey().equals(entry.getValue())
                            ? entry.getKey()
                            : entry.getKey() + " as " + entry.getValue())
                    .toList();

            if (!specifiers.isEmpty()) {
                lines.add("import " + (typeOnly ? "type " : "") + "{ "
                        + String.join(", ", specifiers) + " } from '" + path
                        + "';");
            }
        }
    }
}
