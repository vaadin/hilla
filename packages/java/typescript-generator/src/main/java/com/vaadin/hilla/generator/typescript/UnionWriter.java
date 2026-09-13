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

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.vaadin.hilla.generator.model.UnionModel;

/**
 * Writes the file saying which of its subtypes a value of a polymorphic type
 * can be, which is what lets TypeScript tell them apart by the discriminator.
 */
public final class UnionWriter {
    /**
     * The suffix both the file and the type of a union go by, on top of the
     * name of the type the subtypes belong to.
     */
    public static final String SUFFIX = "Union";

    private static final String UNION = """
            type {{name}} = {{subTypes}};

            export default {{name}};""";

    private static final String SEPARATOR = " | ";

    public GeneratedFile write(UnionModel union) {
        var name = ModulePaths.entityName(union.javaClass()) + SUFFIX;
        var imports = new ImportRegistry();
        imports.reserve(name);

        var types = new TypeWriter(imports,
                ModulePaths.directoryOf(union.javaClass()));

        var subTypes = union.subTypes().stream()
                .map(member -> writeMember(member, types))
                .collect(Collectors.joining(SEPARATOR));

        var body = Template.of(UNION) //
                .with("name", name) //
                .with("subTypes", subTypes) //
                .fill();

        var lines = new ArrayList<>(imports.write());
        lines.add("");
        lines.add(body);

        return new GeneratedFile(ModulePaths.fileOf(union.javaClass() + SUFFIX),
                String.join("\n", lines) + "\n");
    }

    /**
     * Writes one of the types a value can be, narrowed to the value of the
     * discriminator telling it from the subtypes below it when it accepts
     * theirs as well.
     */
    private static String writeMember(UnionModel.Member member,
            TypeWriter types) {
        var written = types.writeRequired(member.type());

        return member.narrowedTo()
                .map(discriminator -> "(" + written + " & { "
                        + Names.property(discriminator.name()) + ": '"
                        + discriminator.acceptedValues().get(0) + "' })")
                .orElse(written);
    }

}
