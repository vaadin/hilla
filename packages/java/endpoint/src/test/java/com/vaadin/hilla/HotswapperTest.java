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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Verifies what a change to a class while the application is running leads to,
 * which is the TypeScript of the endpoints being written again where the change
 * touches what it is written from.
 */
public class HotswapperTest {
    private static final String PERSON = "com.example.Person";

    @Test
    public void should_WriteTheTypeScriptAgainWhenAChangeTouchesAnEndpoint() {
        var generator = generatorUsing(PERSON);

        try (var generators = mockGetInstance(generator)) {
            Hotswapper.onHotswap(true, new String[] { PERSON });
        }

        Mockito.verify(generator).update(PERSON);
    }

    @Test
    public void should_LeaveTheTypeScriptAloneWhenNothingOfTheEndpointsChanged() {
        var generator = generatorUsing(PERSON);

        try (var generators = mockGetInstance(generator)) {
            Hotswapper.onHotswap(true,
                    new String[] { "com.example.SomethingElse" });
        }

        Mockito.verify(generator, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void should_SurviveAClassWhichCannotBeWalked() {
        var generator = Mockito.mock(EndpointCodeGenerator.class);
        Mockito.doThrow(new IllegalStateException(
                "The class is in the middle of being written")).when(generator)
                .getClassesUsedInEndpoints();

        try (var generators = mockGetInstance(generator)) {
            // The next change is another chance at the class, so a development
            // server which stops here would be worse than one which carries on
            assertDoesNotThrow(
                    () -> Hotswapper.onHotswap(true, new String[] { PERSON }),
                    "A class the parser refuses is left to the next change");
        }
    }

    private static EndpointCodeGenerator generatorUsing(String... classes) {
        var generator = Mockito.mock(EndpointCodeGenerator.class);

        Mockito.doReturn(Optional.of(Set.of(classes))).when(generator)
                .getClassesUsedInEndpoints();

        return generator;
    }

    private static MockedStatic<EndpointCodeGenerator> mockGetInstance(
            EndpointCodeGenerator generator) {
        var generators = Mockito.mockStatic(EndpointCodeGenerator.class);

        generators.when(EndpointCodeGenerator::getInstance)
                .thenReturn(generator);

        return generators;
    }
}
