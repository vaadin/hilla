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
package dev.hilla.generator.endpoints.nonnullable;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.hilla.Endpoint;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;

@Endpoint
public class NonNullableEndpoint {

    public int getNonNullableIndex() {
        return 0;
    }

    @Nonnull
    public String getNonNullableString(String input) {
        return "";
    }

    public NonNullableModel echoNonNullModel(
            @Nonnull NonNullableModel[] nonNullableModels) {
        return new NonNullableModel();
    }

    @Nonnull
    public Map<String, NonNullableModel> echoMap(boolean shouldBeNotNull) {
        return Collections.emptyMap();
    }

    @Nonnull
    public Map<String, @NonNull NonNullableModel> echoNonNullableMap(
            @Nonnull List<@NonNull String> nonNullableList) {
        return Collections.emptyMap();
    }

    @dev.hilla.Nonnull
    public Map<String, @dev.hilla.Nonnull VaadinNonNullableModel> echoVaadinNonNullableMap(
            @dev.hilla.Nonnull List<@dev.hilla.Nonnull String> nonNullableParameter) {
        return Collections.emptyMap();
    }

    public NonNullableEndpoint.ReturnType getNotNullReturnType() {
        return new ReturnType();
    }

    public void sendParameterType(
            NonNullableEndpoint.ParameterType parameterType) {
    }

    public String stringNullable() {
        return "";
    }

    @dev.hilla.Nonnull
    public Page<@dev.hilla.Nonnull String> returnNonnullMappedType() {
        return Mockito.mock(Page.class);
    }

    public static class NonNullableModel {
        int[] integers;
        List<Integer> integersList;
        @Nonnull
        String foo;
        int shouldBeNotNullByDefault;
        int first, second, third;
        Optional<Integer> nullableInteger;
        List<@NonNull Map<String, @NonNull String>> listOfMapNullable;
        List<Map<String, String>> listOfMapNullableNotNull;
    }

    public static class VaadinNonNullableModel {
        @dev.hilla.Nonnull
        String foo;
        @dev.hilla.Nonnull
        List<@dev.hilla.Nonnull Integer> nonNullableList;
        @dev.hilla.Nonnull
        Map<String, @dev.hilla.Nonnull String> nonNullableMap;
    }

    public static class ReturnType {
        @NonNull
        String foo;
    }

    public static class ParameterType {
        @Nonnull
        String foo;
    }
}
