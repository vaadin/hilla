package dev.hilla.parser.plugins.transfertypes;

import static dev.hilla.parser.plugins.transfertypes.TransferTypesPluginUtils.createMapper;

import java.util.UUID;

import dev.hilla.parser.core.ClassMappers;

final class UUIDReplacer implements Replacer {
    private ClassMappers classMappers;

    @Override
    public void process() {
        classMappers.add(createMapper(UUID.class, String.class));
    }

    @Override
    public void setClassMappers(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }
}
