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
package com.vaadin.hilla.parser.core.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.testutils.ResourceLoader;
import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

public class BasicTests {
    private static final List<String> STEPS = new LinkedList<>();

    static {
        STEPS.add("-> Root(List)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(List)/Entity(Sample)");
        STEPS.add("-> Root(List)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("<- Root(List)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("-> Root(List)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(List)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(List)/Entity(Sample)");
        STEPS.add("<- Root(List)");
    }

    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private final List<Class<?>> endpoints = List.of(BasicEndpoint.class);

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_TraverseInConsistentOrder() {
        var openAPI = new Parser().classPath(classPath)
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BasicPlugin()).execute(endpoints);

        // The list of endpoints seems to be serialized as "List12". The
        // replacement tries to accommodate for similar representations.
        assertEquals(String.join("\n", STEPS),
                ((String) openAPI.getExtensions()
                        .get(BasicPlugin.FOOTSTEPS_STORAGE_KEY))
                        .replaceAll("List\\w*", "List"));
    }

    @Test
    public void should_UpdateNodesAndCollectNames() {
        var openAPI = new Parser().classPath(classPath)
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BasicPlugin()).execute(endpoints);

        assertEquals(String.join(", ",
                List.of("FieldInfoModel foo", "FieldInfoModel fieldFoo",
                        "FieldInfoModel fieldBar", "MethodInfoModel methodFoo",
                        "MethodInfoModel methodBar")),
                openAPI.getExtensions().get(BasicPlugin.STORAGE_KEY));
    }
}
