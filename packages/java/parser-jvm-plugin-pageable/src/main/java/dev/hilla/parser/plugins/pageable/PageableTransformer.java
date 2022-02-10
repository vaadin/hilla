package dev.hilla.parser.plugins.pageable;

import java.util.List;
import java.util.stream.Stream;

import dev.hilla.parser.core.RelativeClassInfo;
import dev.hilla.parser.core.SharedStorage;

class PageableTransformer {
    private final SharedStorage storage;

    public PageableTransformer(SharedStorage storage) {
        this.storage = storage;
    }

    public Stream<RelativeClassInfo> transform(
            Stream<RelativeClassInfo> stream) {
        return stream.map(cls -> {
            switch (cls.get().getName()) {
            case "org.springframework.data.domain.Sort":
                return load("dev.hilla.runtime.transfertypes.Sort");
            case "org.springframework.data.domain.Pageable":
                return load("dev.hilla.runtime.transfertypes.Pageable");
            case "org.springframework.data.domain.Page":
                return load(List.class.getName());
            case "org.springframework.data.domain.Sort$Order":
                return load("dev.hilla.runtime.transfertypes.Order");
            default:
                return cls;
            }
        });
    }

    private RelativeClassInfo load(String name) {
        return new RelativeClassInfo(
                storage.getScanResult().getClassInfo(name));
    }
}
