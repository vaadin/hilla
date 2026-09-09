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
package com.vaadin.hilla.parser.plugins.model;

import org.jspecify.annotations.NonNull;

/**
 * A reference to a JVM type, used for annotation attributes that hold a class
 * rather than a plain value.
 */
public final class JvmTypeRef {
    private final String jvmType;

    public JvmTypeRef(@NonNull String jvmType) {
        this.jvmType = jvmType;
    }

    @NonNull
    public String getJvmType() {
        return jvmType;
    }
}
