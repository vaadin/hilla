/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * A set of static methods used in CCDM generators, so as flow do not depend on
 * external libraries for these operations.
 */
final class GeneratorUtils {
    
    private GeneratorUtils() {
    }

    static boolean equals(String s1, String s2) {
        return s1 == s2 || s1 != null && s1.equals(s2);
    }

    static int compare(String s1, String s2) {
        return equals(s1, s2) ? 0
                : s1 == null ? -1 : s2 == null ? 1 : s1.compareTo(s2);
    }

    static String capitalize(String s) {
        return s == null ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static boolean isBlank(String s) {
        return s == null || s.replaceAll("\\s+","").isEmpty();
    }

    static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    static String firstNonBlank(String... values) {
        return Arrays.stream(values).filter(GeneratorUtils::isNotBlank)
                .findFirst().orElse(null);
    }

    static boolean isTrue(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    static boolean isNotTrue(Boolean b) {
        return !isTrue(b);
    }

    static <T> T defaultIfNull(T o, T def) {
        return o != null ? o : def;
    }
    
    static String replaceChars(String s, char c1, final char c2) {
        return s == null ? s : s.replace(c1, c2);
    }
    
    @SuppressWarnings("squid:S2259")
    static boolean contains(String s, String p) {
        return isNotBlank(s) && isNotBlank(p) && s.contains(p);
    }

    @SuppressWarnings("squid:S2259")
    static String substringAfter(String s, String p) {
        return contains(s, p) ? s.substring(s.indexOf(p) + p.length()) : "";
    }

    @SuppressWarnings("squid:S2259")
    static String substringAfterLast(String s, String p) {
        return contains(s, p) ? s.substring(s.lastIndexOf(p) + p.length()) : "";
    }

    @SuppressWarnings("squid:S2259")
    static String substringBeforeLast(String s, String p) {
        return contains(s, p) ? s.substring(0, s.lastIndexOf(p)) : s;
    }

    @SuppressWarnings("squid:S2259")
    static boolean endsWith(String s, String p) {
        return contains(s, p) && s.length() == p.length() + s.lastIndexOf(p);
    }

    @SuppressWarnings("squid:S2259")
    static String removeEnd(String s, String p) {
        return endsWith(s, p) ? s.substring(0, s.lastIndexOf(p)) : s;
    }

    static boolean hasAnnotation(NodeWithAnnotations<?> declaration, CompilationUnit compilationUnit,
            Class<? extends Annotation> annotation) {
        Optional<AnnotationExpr> endpointAnnotation = declaration.getAnnotationByClass(annotation);
        if (endpointAnnotation.isPresent()) {
            return compilationUnit.getImports().stream()
                    .anyMatch(importDeclaration -> annotation.getName().equals(importDeclaration.getNameAsString())); // NOSONAR
        }
        return false;
    }
}
