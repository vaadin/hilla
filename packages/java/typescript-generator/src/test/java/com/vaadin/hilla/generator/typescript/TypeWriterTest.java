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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.model.TypeModel;

/**
 * Covers what types turn into where no endpoint signature reaches by itself:
 * the type parameters the entities declare and use, and the names of the types
 * the browser has, however deep a type holds them.
 */
public class TypeWriterTest {
    private final ImportRegistry imports = new ImportRegistry();
    private final TypeWriter writer = new TypeWriter(imports, "");

    @Test
    public void should_WriteATypeVariableAsTheNameItIsDeclaredUnder() {
        assertEquals("T", writer.write(TypeModel.TypeVariable.of("T")));
    }

    @Test
    public void should_WriteWhatAGenericEntityIsUsedWith() {
        var box = new TypeModel.EntityRef("com.example.Box",
                List.of(TypeModel.TypeVariable.of("T"),
                        new TypeModel.Scalar(TypeModel.ScalarKind.STRING, true,
                                String.class.getName())),
                true);

        assertEquals("Box<T, string | undefined> | undefined",
                writer.write(box));
        assertEquals(List.of("import type Box from './com/example/Box.js';"),
                imports.write());
    }

    @Test
    public void should_KeepTheNameOfATypeTheBrowserHasWhereverItIsHeld() {
        // However deep the type the browser has is held, the type of the
        // application named after it is imported under another name
        var file = new TypeModel.Provided("File", "", List.of(), true);
        var holders = List.<TypeModel> of(
                new TypeModel.ArrayOf(file, true, "java.util.List"),
                new TypeModel.MapOf(file, true, "java.util.Map"),
                new TypeModel.EntityRef("com.example.Box", List.of(file), true),
                new TypeModel.Provided("Signal", "@vaadin/hilla-react-signals",
                        List.of(file), true));

        for (var holder : holders) {
            var registry = new ImportRegistry();
            new TypeWriter(registry, "").reserveProvided(holder);

            assertEquals("File_1",
                    new TypeWriter(registry, "")
                            .write(TypeModel.EntityRef.of("com.example.File")),
                    holder::toString);
        }
    }
}
