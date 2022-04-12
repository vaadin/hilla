package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;
import dev.hilla.parser.models.ClassInfoModel;

final class TransferTypesPluginUtils {
    public static ClassMappers.Mapper createMapper(String from, Class<?> to) {
        return cls -> cls.is(from) ? ClassInfoModel.of(to) : cls;
    }

    public static ClassMappers.Mapper createMapper(Class<?> from, Class<?> to) {
        return createMapper(from.getName(), to);
    }
}
