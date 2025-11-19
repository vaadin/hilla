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
package com.vaadin.hilla.parser.plugins.nonnull.nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.nonnull.AnnotationMatcher;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPluginConfig;
import com.vaadin.hilla.parser.plugins.nonnull.nullable.nonNullApi.NullableNonNullEndpoint;
import com.vaadin.hilla.parser.plugins.nonnull.test.helpers.TestHelper;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

public class NullableTest {
    private final TestHelper helper = new TestHelper(getClass());

    @Test
    public void should_ApplyNullableAnnotation()
            throws IOException, URISyntaxException {
        var plugin = new NonnullPlugin();
        plugin.setConfiguration(new NonnullPluginConfig());

        var openAPI = new Parser()
                .classPath(Set.of(helper.getTargetDir().toString()))
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BackbonePlugin()).addPlugin(plugin)
                .execute(List.of(NullableEndpoint.class,
                        NullableNonNullEndpoint.class));

        helper.executeParserWithConfig(openAPI);
    }

    @Test
    public void annotationMatcher_shouldHaveDefaultConstructorAndSetter() {
        // to enable maven initialize instances of AnnotationMatcher from
        // pom.xml configurations, properly, it should have the default
        // constructor and setter methods:
        AnnotationMatcher annotationMatcher = new AnnotationMatcher();
        annotationMatcher.setName("name");
        annotationMatcher.setScore(100);
        annotationMatcher.setMakesNullable(true);
        Assertions.assertEquals("name", annotationMatcher.getName());
        Assertions.assertEquals(100, annotationMatcher.getScore());
        Assertions.assertTrue(annotationMatcher.doesMakeNullable());
    }
}
