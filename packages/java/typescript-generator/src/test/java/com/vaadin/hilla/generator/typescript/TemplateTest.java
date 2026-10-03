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
package com.vaadin.hilla.generator.typescript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TemplateTest {
    @Test
    public void should_FillEveryHoleWithTheValueOfItsName() {
        assertEquals("const client = new ConnectClient();",
                Template.of("const {{name}} = new {{type}}();")
                        .with("name", "client").with("type", "ConnectClient")
                        .fill());
    }

    @Test
    public void should_FillAHoleUsedMoreThanOnce() {
        assertEquals("client = client", Template.of("{{name}} = {{name}}")
                .with("name", "client").fill());
    }

    @Test
    public void should_LeaveTheBracesOfTheTypeScriptAlone() {
        assertEquals("new ConnectClient({ prefix: 'connect' });",
                Template.of("new {{type}}({ prefix: 'connect' });")
                        .with("type", "ConnectClient").fill());
    }

    @Test
    public void should_TakeAValueWhichLooksLikeAReplacement() {
        assertEquals("const name = $0;", Template.of("const name = {{value}};")
                .with("value", "$0").fill());
    }

    @Test
    public void should_RefuseAHoleWithoutAValue() {
        var template = Template.of("{{known}} and {{forgotten}}").with("known",
                "one");

        var thrown = assertThrows(IllegalArgumentException.class,
                template::fill);

        assertEquals("Nothing to fill {{forgotten}} with", thrown.getMessage());
    }
}
