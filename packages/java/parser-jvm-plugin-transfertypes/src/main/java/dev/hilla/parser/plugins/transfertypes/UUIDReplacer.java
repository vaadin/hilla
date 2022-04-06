package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;

import java.util.UUID;

import static dev.hilla.parser.plugins.transfertypes.TransferTypesPluginUtils.createReplacer;

final class UUIDReplacer implements Replacer {
    private ClassMappers classMappers;

    @Override
    public void setClassMappers(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }

    @Override
    public void process() {
        classMappers.add(createReplacer(UUID.class, String.class));
    }
}
