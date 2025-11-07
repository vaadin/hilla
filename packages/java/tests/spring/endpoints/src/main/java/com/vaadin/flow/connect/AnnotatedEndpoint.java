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
package com.vaadin.flow.connect;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

/**
 * Endpoint to verify Jackson annotations support.
 */
@Endpoint
@AnonymousAllowed
public class AnnotatedEndpoint {
    public AnnotatedEntity getAnnotatedEntity() {
        return new AnnotatedEntity();
    }

    /**
     * Bean annotated with Jackson annotations.
     */
    public static class AnnotatedEntity {
        @NotBlank
        private String defaultName = "value";

        @JsonProperty("customName")
        @NonNull
        public String getDefaultName() {
            return "value";
        }

        @JsonProperty("customName")
        public void setDefaultName(@NonNull String defaultName) {
            this.defaultName = defaultName;
        }
    }
}
