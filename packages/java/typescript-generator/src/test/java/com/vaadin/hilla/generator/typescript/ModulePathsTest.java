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

import org.junit.jupiter.api.Test;

public class ModulePathsTest {
    @Test
    public void should_MirrorThePackageOfTheEntity() {
        assertEquals("./com/example/Person.js",
                ModulePaths.forEntity("com.example.Person", ""));
    }

    @Test
    public void should_TreatNestedClassesAsFolders() {
        assertEquals("./com/example/Person/Address.js",
                ModulePaths.forEntity("com.example.Person$Address", ""));
        assertEquals("Address",
                ModulePaths.entityName("com.example.Person$Address"));
        assertEquals("Person", ModulePaths.entityName("com.example.Person"));
    }

    @Test
    public void should_ClimbOutOfTheFolderOfTheFileBeingWritten() {
        assertEquals("./Address.js",
                ModulePaths.forEntity("com.example.Address", "com/example"));
        assertEquals("../Person.js",
                ModulePaths.forEntity("com.Person", "com/example"));
        assertEquals("../../../PersonEndpoint.js",
                ModulePaths.forEndpoint("PersonEndpoint", "com/example/data"));
    }

    @Test
    public void should_StepDownIntoTheFolderOfTheEntity() {
        assertEquals("./data/Address.js", ModulePaths
                .forEntity("com.example.data.Address", "com/example"));
    }
}
