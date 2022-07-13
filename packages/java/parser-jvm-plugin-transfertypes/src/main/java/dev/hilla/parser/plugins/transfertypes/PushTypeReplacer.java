package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;
import dev.hilla.runtime.transfertypes.EndpointSubscription;
import dev.hilla.runtime.transfertypes.Flux;

public class PushTypeReplacer implements Replacer {
    private ClassMappers classMappers;

    @Override
    public void process() {
        classMappers.add(TransferTypesPluginUtils
                .createMapper("reactor.core.publisher.Flux", Flux.class));
        classMappers.add(TransferTypesPluginUtils.createMapper(
                "dev.hilla.EndpointSubscription", EndpointSubscription.class));
    }

    @Override
    public void setClassMappers(ClassMappers classMappers) {
        this.classMappers = classMappers;
    }
}
