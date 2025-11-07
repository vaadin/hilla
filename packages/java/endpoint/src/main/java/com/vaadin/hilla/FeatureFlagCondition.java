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
package com.vaadin.hilla;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.di.ResourceProvider;

/**
 * Checks if a given feature flag, defined using
 * {@link ConditionalOnFeatureFlag}, is enabled.
 */
public class FeatureFlagCondition implements Condition {

    private FeatureFlags featureFlags = null;

    @Override
    public boolean matches(ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        if (context.getEnvironment()
                .getProperty(FeatureFlagCondition.class.getName()
                        + ".alwaysEnable") != null) {
            // This is only for testing
            return true;
        }
        if (featureFlags == null) {
            featureFlags = getFeatureFlags(context);
        }
        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(
                        ConditionalOnFeatureFlag.class.getName());
        if (annotationAttributes == null) {
            throw new IllegalArgumentException(
                    getClass().getName() + " can only be used through an @"
                            + ConditionalOnFeatureFlag.class.getSimpleName()
                            + " annotation and none was found");
        }
        String featureId = (String) annotationAttributes.get("value");
        return featureFlags.isEnabled(featureId);
    }

    private FeatureFlags getFeatureFlags(ConditionContext context) {
        ClassLoader classLoader = Objects
                .requireNonNull(context.getClassLoader());

        ResourceProvider provider = new ResourceProvider() {

            @Override
            public URL getApplicationResource(String path) {
                return classLoader.getResource(path);
            }

            @Override
            public List<URL> getApplicationResources(String path)
                    throws IOException {
                return Collections.list(classLoader.getResources(path));
            }

            @Override
            public URL getClientResource(String path) {
                return getApplicationResource(path);
            }

            @Override
            public InputStream getClientResourceAsStream(String path)
                    throws IOException {
                return getClientResource(path).openStream();
            }
        };
        Lookup lookup = Lookup.of(provider, ResourceProvider.class);
        return new FeatureFlags(lookup);
    }
}
