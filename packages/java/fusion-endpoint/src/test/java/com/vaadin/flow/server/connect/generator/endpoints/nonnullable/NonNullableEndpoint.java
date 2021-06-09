/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator.endpoints.nonnullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.server.connect.Endpoint;

@Endpoint
public class NonNullableEndpoint {

    @Nonnull
    public String getNullableString(String input) {
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

    public NonNullableEndpoint.ReturnType getNotNullReturnType() {
        return new ReturnType();
    }

    public void sendParameterType(
            NonNullableEndpoint.ParameterType parameterType) {
    }

    public String stringNullable() {
        return "";
    }

    public static class NonNullableModel {
        int[] integers;
        List<Integer> integersList;
        @Nonnull
        String foo;
        int shouldBeNotNullByDefault;
        int first, second, third;
        Optional<Integer> nullableInteger;
        List<Map<String, String>> listOfMapNullable;
        List<Map<String, String>> listOfMapNullableNotNull;
    }

    public static class ReturnType {
        @Nonnull
        String foo;
    }

    public static class ParameterType {
        @Nonnull
        String foo;
    }
}
