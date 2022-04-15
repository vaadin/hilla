package dev.hilla.parser.plugins.transfertypes;

import static dev.hilla.parser.plugins.transfertypes.TransferTypesPluginUtils.createMapper;

import java.util.List;

import dev.hilla.parser.core.ClassMappers;
import dev.hilla.runtime.transfertypes.Order;
import dev.hilla.runtime.transfertypes.Pageable;
import dev.hilla.runtime.transfertypes.Sort;

final class PageableReplacer implements Replacer {
    private ClassMappers classMappers;

    @Override
    public void process() {
        classMappers.add(TransferTypesPluginUtils.createMapper(
                "org.springframework.data.domain.Sort", Sort.class));
        classMappers.add(TransferTypesPluginUtils.createMapper(
                "org.springframework.data.domain.Pageable", Pageable.class));
        classMappers.add(TransferTypesPluginUtils.createMapper(
                "org.springframework.data.domain.Page", List.class));
        classMappers.add(TransferTypesPluginUtils.createMapper(
                "org.springframework.data.domain.Sort$Order", Order.class));
    }

    @Override
    public void setClassMappers(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }
}
