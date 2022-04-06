package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;
import dev.hilla.parser.models.ClassInfoModel;

final class TransferTypesPluginUtils {
    public static ClassMappers.Mapper createReplacer(String from, Class<?> to) {
        return cls -> cls.is(from) ? ClassInfoModel.of(to) : cls;
    }

    public static ClassMappers.Mapper createReplacer(Class<?> from,
            Class<?> to) {
        return createReplacer(from.getName(), to);
    }
}
