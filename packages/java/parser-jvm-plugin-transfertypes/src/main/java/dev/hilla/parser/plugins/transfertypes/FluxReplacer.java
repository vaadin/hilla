package dev.hilla.parser.plugins.transfertypes;

import java.util.List;

import dev.hilla.parser.core.ClassMappers;

public class FluxReplacer implements Replacer {
    private ClassMappers classMappers;

    @Override
    public void process() {
        classMappers.add(TransferTypesPluginUtils
                .createMapper("reactor.core.publisher.Flux", List.class));
        classMappers.add(TransferTypesPluginUtils
                .createMapper("dev.hilla.EndpointSubscription", List.class));
    }

    @Override
    public void setClassMappers(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }
}
