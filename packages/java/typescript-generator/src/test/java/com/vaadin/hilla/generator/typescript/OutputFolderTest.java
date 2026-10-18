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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OutputFolderTest {
    private static final FileTime EARLIER = FileTime
            .from(Instant.parse("2025-01-01T00:00:00Z"));

    @TempDir
    private Path folder;

    private OutputFolder output;

    @BeforeEach
    public void createOutputFolder() {
        output = new OutputFolder(folder);
    }

    @Test
    public void should_WriteEveryFileAndTheListOfThem() throws IOException {
        output.write(List.of(new GeneratedFile("endpoints.ts", "export {};\n"),
                new GeneratedFile("com/example/Person.ts",
                        "interface P {}\n")));

        assertEquals("export {};\n", read("endpoints.ts"));
        assertEquals("interface P {}\n", read("com/example/Person.ts"));
        assertEquals("endpoints.ts\ncom/example/Person.ts\n",
                read(OutputFolder.FILE_LIST));
    }

    @Test
    public void should_WriteOnlyTheFilesWhichChanged() throws IOException {
        output.write(List.of(new GeneratedFile("endpoints.ts", "export {};\n"),
                new GeneratedFile("Person.ts", "interface P {}\n")));
        touch("endpoints.ts");
        touch("Person.ts");

        output.write(List.of(new GeneratedFile("endpoints.ts", "export {};\n"),
                new GeneratedFile("Person.ts", "interface Person {}\n")));

        assertEquals(EARLIER,
                Files.getLastModifiedTime(folder.resolve("endpoints.ts")),
                "The file did not change, so watching the folder should not"
                        + " see it as written");
        assertEquals("interface Person {}\n", read("Person.ts"));
    }

    @Test
    public void should_RemoveWhatTheRunBeforeItGeneratedAndNothingElse()
            throws IOException {
        output.write(List.of(new GeneratedFile("endpoints.ts", "export {};\n"),
                new GeneratedFile("com/example/Person.ts",
                        "interface P {}\n")));
        Files.writeString(folder.resolve("of-the-application.ts"), "// mine\n");

        output.write(
                List.of(new GeneratedFile("endpoints.ts", "export {};\n")));

        assertFalse(Files.exists(folder.resolve("com/example/Person.ts")),
                "The type is no longer generated");
        assertFalse(Files.exists(folder.resolve("com")),
                "The folder of the type is left empty by it");
        assertTrue(Files.exists(folder.resolve("of-the-application.ts")),
                "The generator did not write it");
    }

    @Test
    public void should_LeaveNoListBehindWhenThereIsNothingToGenerate()
            throws IOException {
        output.write(List.of(new GeneratedFile("endpoints.ts", "export {};\n"),
                new GeneratedFile("com/example/Person.ts",
                        "interface P {}\n")));

        output.write(List.of());

        assertFalse(Files.exists(folder.resolve("endpoints.ts")),
                "There is nothing to call");
        assertFalse(Files.exists(folder.resolve(OutputFolder.FILE_LIST)),
                "Nothing in the folder was generated");
    }

    @Test
    public void should_RemoveNothingWhenItHasNeverGeneratedHereBefore()
            throws IOException {
        Files.writeString(folder.resolve("of-the-application.ts"), "// mine\n");

        output.write(
                List.of(new GeneratedFile("endpoints.ts", "export {};\n")));

        assertTrue(Files.exists(folder.resolve("of-the-application.ts")));
    }

    private String read(String path) throws IOException {
        return Files.readString(folder.resolve(path));
    }

    private void touch(String path) throws IOException {
        Files.setLastModifiedTime(folder.resolve(path), EARLIER);
    }
}
