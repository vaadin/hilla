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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class OpenAPIUtilTest {

    @Test
    public void emptySchemaReturnsNoComponents() throws IOException {
        Assert.assertEquals(Collections.emptySet(),
                parse("openapi-noendpoints.json"));
    }

    @Test
    public void noComponentsReturnEndpointTypes() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint"),
                parse("openapi-nocustomtypes.json"));
    }

    @Test
    public void singleType() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-customtype.json"));
    }

    @Test
    public void referringTypes() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.MyType",
                "com.example.application.endpoints.helloreact.MyOtherType"),
                parse("openapi-referring-customtypes.json"));
    }

    @Test
    public void nestedType() throws IOException {
        Assert.assertEquals(Set.of(
                "com.example.application.endpoints.helloreact.HelloReactEndpoint",
                "com.example.application.endpoints.helloreact.HelloReactEndpoint$MyInnerType"),
                parse("openapi-innertype.json"));
    }

    private Set<String> parse(String openapiFilename) throws IOException {
        String openApi = IOUtils.toString(
                getClass().getResourceAsStream(openapiFilename),
                StandardCharsets.UTF_8);
        return OpenAPIUtil.findOpenApiClasses(openApi);
    }

}
