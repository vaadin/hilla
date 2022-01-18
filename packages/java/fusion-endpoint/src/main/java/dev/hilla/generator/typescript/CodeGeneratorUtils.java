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
package dev.hilla.generator.typescript;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.hilla.generator.GeneratorUtils;

import static dev.hilla.generator.typescript.CodeGenerator.IMPORT;

class CodeGeneratorUtils {
    private CodeGeneratorUtils() {
    }

    // Method for extracting fully qualified name in a complex type. E.g.
    // 'com.example.mypackage.Bean' will be extracted in the type
    // `Map<String, Map<String, com.example.mypackage.Bean>>`
    static String getSimpleNameFromComplexType(String dataType,
            List<Map<String, String>> imports) {
        return TypeParser.parse(dataType).traverse()
                .visit(new SimpleNameVisitor(imports)).finish().toString();
    }

    static String getSimpleNameFromImports(String dataType,
            List<Map<String, String>> imports) {
        for (Map<String, String> anImport : imports) {
            if (Objects.equals(dataType, anImport.get(IMPORT))) {
                return GeneratorUtils.firstNonBlank(anImport.get("importAs"),
                        anImport.get("className"));
            }
        }
        if (GeneratorUtils.contains(dataType, "<")
                || GeneratorUtils.contains(dataType, "{")
                || GeneratorUtils.contains(dataType, "|")) {
            return getSimpleNameFromComplexType(dataType, imports);
        }
        return getSimpleNameFromQualifiedName(dataType);
    }

    static String getSimpleNameFromQualifiedName(String qualifiedName) {
        if (GeneratorUtils.contains(qualifiedName, ".")) {
            return GeneratorUtils.substringAfterLast(qualifiedName, ".");
        }
        return qualifiedName;
    }

    static class SimpleNameVisitor implements TypeParser.Visitor {
        private final List<Map<String, String>> imports;

        SimpleNameVisitor(List<Map<String, String>> imports) {
            this.imports = imports;
        }

        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            String name = node.getName();

            if (name.contains(".")) {
                node.setName(getSimpleNameFromImports(name, imports));
            }

            return node;
        }
    }
}
