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

package dev.hilla.generator.endpoints.iterableendpoint;

import dev.hilla.generator.endpoints.AbstractEndpointGenerationTest;

import org.junit.Test;

import java.util.Collections;

/**
 * IterableEndpointGenerationTest, created on 21/12/2020 23.00
 *
 * @author nikolaigorokhov
 */
public class IterableEndpointGenerationTest
        extends AbstractEndpointGenerationTest {

    public IterableEndpointGenerationTest() {
        super(Collections.singletonList(IterableEndpoint.class));
    }

    @Test
    public void should_ConvertIterableIntoArrayInTS() {
        verifyOpenApiObjectAndGeneratedTs();
    }
}
