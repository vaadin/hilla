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
package com.vaadin.hilla.parser.plugins.nonnull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationMatcherTest {
    @Test
    public void should_HaveDefaultConstructorAndSetters() {
        // to enable maven initialize instances of AnnotationMatcher from
        // pom.xml configurations, properly, it should have the default
        // constructor and setter methods:
        var annotationMatcher = new AnnotationMatcher();
        annotationMatcher.setName("name");
        annotationMatcher.setScore(100);
        annotationMatcher.setMakesNullable(true);

        Assertions.assertEquals("name", annotationMatcher.getName());
        Assertions.assertEquals(100, annotationMatcher.getScore());
        Assertions.assertTrue(annotationMatcher.doesMakeNullable());
    }
}
