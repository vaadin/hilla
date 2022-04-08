package dev.hilla.parser.plugins.transfertypes;

import static dev.hilla.parser.plugins.transfertypes.TransferTypesPluginUtils.createReplacer;

import java.util.List;

import dev.hilla.parser.core.ClassMappers;
import dev.hilla.runtime.transfertypes.Order;
import dev.hilla.runtime.transfertypes.Pageable;
import dev.hilla.runtime.transfertypes.Sort;

public class PageableReplacer {
    private final ClassMappers classMappers;

    public PageableReplacer(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }

    public void process() {
        classMappers.add(createReplacer("org.springframework.data.domain.Sort",
                Sort.class));
        classMappers.add(createReplacer(
                "org.springframework.data.domain.Pageable", Pageable.class));
        classMappers.add(createReplacer("org.springframework.data.domain.Page",
                List.class));
        classMappers.add(createReplacer(
                "org.springframework.data.domain.Sort$Order", Order.class));
    }
}
