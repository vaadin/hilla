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
package com.vaadin.fusion.generator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * A set of static methods used in CCDM generators, so as flow do not depend on
 * external libraries for these operations.
 */
public final class GeneratorUtils {

    private GeneratorUtils() {
    }

    /**
     * Capitalizes the string.
     *
     * @param s
     *            string to capitalize.
     * @return result of capitalization.
     */
    public static String capitalize(String s) {
        return s == null ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Compares two strings lexicographically.
     *
     * @see String#compareTo
     *
     * @param s1
     *            first string.
     * @param s2
     *            second string.
     * @return result of comparison. Can be -1, 0, 1.
     */
    public static int compare(String s1, String s2) {
        return Objects.equals(s1, s2) ? 0
                : s1 == null ? -1 : s2 == null ? 1 : s1.compareTo(s2);
    }

    /**
     * Checks if the string s contains string p with an additional check for
     * them to not be blank.
     *
     * @param s
     *            a string where search to be performed.
     * @param p
     *            a string to be used for search.
     * @return the result of a check.
     */
    @SuppressWarnings("squid:S2259")
    public static boolean contains(String s, String p) {
        return isNotBlank(s) && isNotBlank(p) && s.contains(p);
    }

    /**
     * Searches a first non-blank string in the received arguments.
     *
     * @param values
     *            a vararg array to search within.
     * @return a result of the search.
     */
    public static String firstNonBlank(String... values) {
        return Arrays.stream(values).filter(GeneratorUtils::isNotBlank)
                .findFirst().orElse(null);
    }

    /**
     * Checks whether the declaration has the specific annotation.
     *
     * @param declaration
     *            a declaration that is checked to have an annotation.
     * @param compilationUnit
     *            a compilation unit.
     * @param annotation
     *            an annotation to check.
     * @return the result of a check.
     */
    public static boolean hasAnnotation(NodeWithAnnotations<?> declaration,
            CompilationUnit compilationUnit,
            Class<? extends Annotation> annotation) {
        Optional<AnnotationExpr> endpointAnnotation = declaration
                .getAnnotationByClass(annotation);
        if (endpointAnnotation.isPresent()) {
            return compilationUnit.getImports().stream()
                    .anyMatch(importDeclaration -> annotation.getName()
                            .equals(importDeclaration.getNameAsString())); // NOSONAR
        }
        return false;
    }

    /**
     * Checks if the string contains only whitespaces.
     *
     * @param s
     *            a string to check.
     * @return a result of a check.
     */
    public static boolean isBlank(String s) {
        return s == null || s.replaceAll("\\s+", "").isEmpty();
    }

    /**
     * Checks if the string contains not only whitespaces.
     *
     * @param s
     *            a string to check.
     * @return a result of a check.
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * Checks a value for being not truth (or null).
     *
     * @param b
     *            a boolean to check.
     * @return a result of a check.
     */
    public static boolean isNotTrue(Boolean b) {
        return !isTrue(b);
    }

    /**
     * Checks a value for being true with an additional null check.
     *
     * @param b
     *            a boolean to check for truth.
     * @return a result of a check.
     */
    public static boolean isTrue(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    /**
     * Removes the end of a string.
     *
     * @param s
     *            a string to remove an end.
     * @param p
     *            an end part of a string.
     * @return a result string.
     */
    @SuppressWarnings("squid:S2259")
    public static String removeEnd(String s, String p) {
        return s.endsWith(p) ? s.substring(0, s.lastIndexOf(p)) : s;
    }

    /**
     * Replaces one char with another in a string with an additional null check.
     *
     * @param s
     *            a string to perform replace within.
     * @param c1
     *            a char to be replaced.
     * @param c2
     *            a char to be replacement.
     * @return a result string.
     */
    public static String replaceChars(String s, char c1, final char c2) {
        return s == null ? s : s.replace(c1, c2);
    }

    /**
     * Gets the substring of an s string that goes after the first entry of the
     * p string.
     *
     * @param s
     *            a string to get substring of.
     * @param p
     *            a string to be searched.
     * @return a substring.
     */
    @SuppressWarnings("squid:S2259")
    public static String substringAfter(String s, String p) {
        return contains(s, p) ? s.substring(s.indexOf(p) + p.length()) : "";
    }

    /**
     * Gets the substring of an s string that goes after the last entry of the p
     * string.
     *
     * @param s
     *            a string to get substring of.
     * @param p
     *            a string to be searched.
     * @return a substring.
     */
    @SuppressWarnings("squid:S2259")
    public static String substringAfterLast(String s, String p) {
        return contains(s, p) ? s.substring(s.lastIndexOf(p) + p.length()) : "";
    }

    /**
     * Gets the substring of an s string that goes before the last entry of the
     * p string.
     *
     * @param s
     *            a string to get substring of.
     * @param p
     *            a string to be searched.
     * @return a substring.
     */
    @SuppressWarnings("squid:S2259")
    public static String substringBeforeLast(String s, String p) {
        return contains(s, p) ? s.substring(0, s.lastIndexOf(p)) : s;
    }

    /**
     * Runs a lambda against elements of two lists at once.
     *
     * @param first
     *            a first list.
     * @param second
     *            a second list.
     * @param zipper
     *            a lambda function that accepts elements from both lists at
     *            once.
     * @param <P1>
     *            a type of item of a first list.
     * @param <P2>
     *            a type of item of a second list.
     * @param <R>
     *            a type of the streamed result.
     * @return a stream with the values zipper produced.
     */
    public static <P1, P2, R> Stream<R> zip(List<P1> first, List<P2> second,
            BiFunction<P1, P2, R> zipper) {
        return IntStream.range(0, Math.min(first.size(), second.size()))
                .mapToObj(i -> zipper.apply(first.get(i), second.get(i)));
    }
}
