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
package com.vaadin.hilla.engine;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin;
import com.vaadin.hilla.parser.plugins.model.ModelPlugin;
import com.vaadin.hilla.parser.plugins.nonnull.NonnullPlugin;
import com.vaadin.hilla.parser.plugins.subtypes.SubTypesPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.MultipartFileCheckerPlugin;
import com.vaadin.hilla.parser.plugins.transfertypes.TransferTypesPlugin;

public final class ParserConfiguration {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ParserConfiguration.class);
    private List<Class<? extends Annotation>> endpointAnnotations = List.of();
    private List<Class<? extends Annotation>> endpointExposedAnnotations = List
            .of();

    public List<Class<? extends Annotation>> getEndpointAnnotations() {
        return endpointAnnotations;
    }

    public List<Class<? extends Annotation>> getEndpointExposedAnnotations() {
        return endpointExposedAnnotations;
    }

    public void setEndpointAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointAnnotations) {
        this.endpointAnnotations = endpointAnnotations;
    }

    public void setEndpointExposedAnnotations(
            @NonNull List<Class<? extends Annotation>> endpointExposedAnnotations) {
        this.endpointExposedAnnotations = endpointExposedAnnotations;
    }

    /**
     * The class names of the plugins the parser runs, in the order it runs
     * them.
     */
    static final List<String> PLUGINS = createPlugins();

    private static List<String> createPlugins() {
        List<String> plugins = new ArrayList<>();
        // Always include these plugins.
        plugins.add(BackbonePlugin.class.getName());
        plugins.add(MultipartFileCheckerPlugin.class.getName());
        plugins.add(TransferTypesPlugin.class.getName());

        // Conditionally add the Kotlin nullability plugin if available.
        try {
            Class<?> kotlinNullabilityClass = Class.forName(
                    "com.vaadin.hilla.parser.plugins.nonnull.kotlin.KotlinNullabilityPlugin");

            // Check that a class from kotlin-reflect is available:
            Class.forName("kotlin.reflect.KClass");

            plugins.add(kotlinNullabilityClass.getName());
        } catch (Throwable e) {
            LOGGER.debug("Kotlin nullability plugin is not going to be loaded. "
                    + "If you wish to enable it, please ensure that both 'kotlin-reflect' "
                    + "and 'hilla-parser-jvm-plugin-nonnull-kotlin' are in your classpath.");
        }

        // Add the remaining plugins.
        plugins.add(NonnullPlugin.class.getName());
        plugins.add(SubTypesPlugin.class.getName());
        plugins.add(ModelPlugin.class.getName());
        return List.copyOf(plugins);
    }
}
