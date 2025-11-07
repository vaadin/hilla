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
package com.vaadin.hilla.parser.plugins.nonnull.nonnullapi;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vaadin.hilla.parser.plugins.nonnull.nonnullapi.subpackage.SubPackageDependency;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class NonNullApiEndpoint {
    public Dependency defaultMethod(String param) {
        return null;
    }

    public Map<String, Set<Dependency>> nestedSignatureMethod(
            Set<Dependency> param) {
        return null;
    }

    @NullableMethod
    public Dependency nullableMethod(@NullableParameter String param) {
        return null;
    }

    public Map<String, @NullableSignature Set<@NullableSignature Dependency>> nullableNestedSignatureMethod(
            Set<@NullableSignature Dependency> param) {
        return null;
    }

    @NullableSignature
    public Dependency nullableSignature(@NullableSignature String param) {
        return null;
    }

    public static class Dependency {
        public String defaultField;
        @NullableField
        public String nullableField;
        @NullableSignature
        public String nullableSignatureField;
    }

    public Optional<String> optionalMethod(Optional<String> opt) {
        return opt;
    }

    public SubPackageDependency subPackageMethod(SubPackageDependency entity) {
        return entity;
    }

    public static class Generic<T> {
        public T genericField;
    }

    public Generic<String> genericMethod(Generic<String> generic) {
        return generic;
    }

    public Generic<@NullableSignature String> genericNullableMethod(
            Generic<@NullableSignature String> generic) {
        return generic;
    }
}
