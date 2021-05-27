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

package com.vaadin.flow.server.connect.generator.endpoints.reservedwordclass;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.server.connect.generator.endpoints.AbstractEndpointGenerationTest;

public class ReservedWordClassEndpointGenerationTest
        extends AbstractEndpointGenerationTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    public ReservedWordClassEndpointGenerationTest() {
        super(Collections.emptyList());
    }

    @Test
    public void Should_Fail_When_UsingReservedWordInClass() {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("reserved");
        verifyOpenApiObjectAndGeneratedTs();
    }
}
