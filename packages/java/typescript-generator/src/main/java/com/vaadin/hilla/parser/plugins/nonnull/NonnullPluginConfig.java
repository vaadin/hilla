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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.hilla.parser.core.PluginConfiguration;
import com.vaadin.hilla.parser.utils.ConfigList;

public class NonnullPluginConfig
        implements ConfigList<AnnotationMatcher>, PluginConfiguration {
    private final Set<AnnotationMatcher> disable = new HashSet<>();
    private final boolean disableAllDefaults = false;
    private final Set<AnnotationMatcher> use = new HashSet<>();

    public NonnullPluginConfig() {
    }

    public NonnullPluginConfig(Collection<AnnotationMatcher> use,
            Collection<AnnotationMatcher> disable) {
        if (disable != null) {
            this.disable.addAll(disable);
        }

        if (use != null) {
            this.use.addAll(use);
        }
    }

    @Override
    public Collection<AnnotationMatcher> getDisabledOptions() {
        return disable;
    }

    @Override
    public Collection<AnnotationMatcher> getUsedOptions() {
        return use;
    }

    @Override
    public boolean shouldAllDefaultsBeDisabled() {
        return disableAllDefaults;
    }

    static class Processor extends ConfigList.Processor<AnnotationMatcher> {
        static final Set<AnnotationMatcher> defaults = Set.of(
                // Package-level annotations have low score
                new AnnotationMatcher("org.springframework.lang.NonNullApi",
                        false, 10),
                // Id and Version annotation usually mark nullable fields for
                // CRUD operations.
                // Low score allows other annotations to override them.
                new AnnotationMatcher("jakarta.persistence.Id", true, 20),
                new AnnotationMatcher("jakarta.persistence.Version", true, 20),
                // Nullable-like annotations get a higher score. This should
                // only matter when they are used in conjunction with
                // package-level annotations
                new AnnotationMatcher("jakarta.annotation.Nullable", true, 20),
                new AnnotationMatcher("com.vaadin.hilla.Nullable", true, 20),
                new AnnotationMatcher("org.springframework.lang.Nullable", true,
                        20),
                new AnnotationMatcher("org.jspecify.annotations.Nullable", true,
                        20),
                // Nonnull-like annotations have the highest score for
                // compatibility with the old generator
                new AnnotationMatcher("jakarta.annotation.Nonnull", false, 30),
                new AnnotationMatcher("javax.annotation.Nonnull", false, 30),
                new AnnotationMatcher("com.vaadin.hilla.Nonnull", false, 30),
                new AnnotationMatcher("org.springframework.lang.NonNull", false,
                        30),
                new AnnotationMatcher("org.jspecify.annotations.NonNull", false,
                        30));

        public Processor(NonnullPluginConfig config) {
            super(config, defaults);
        }
    }

}
