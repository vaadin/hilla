/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.server.connect.generator.endpoints.nullable;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.server.connect.Endpoint;

@Endpoint
public class NullableEndpoint {

    public String getNullableString(@Nullable String input) {
        return "";
    }

    @Nullable
    public NullableModel echoNonNullMode(NullableModel[] nullableModels) {
        return new NullableModel();
    }

    public Map<String, NullableModel> echoMap(boolean shouldBeNotNull) {
        return Collections.emptyMap();
    }

    @Nullable
    public NullableEndpoint.ReturnType getNotNullReturnType() {
        return new ReturnType();
    }

    public void sendParameterType(
            @Nullable NullableEndpoint.ParameterType parameterType) {

    }

    @Nullable
    public String stringNullable() {
        return "";
    }

    public static class NullableModel {

        String foo;
        String bar;
        int shouldBeNotNullByDefault;
        Optional<Integer> nullableInteger;
        @Nullable
        List<Map<String, String>> listOfMapNullable;
        @Nullable
        List<Map<String, String>> listOfMapNullableNotNull;
    }

    public static class ReturnType {
        String foo;
    }

    public static class ParameterType {
        String foo;
    }
}
