package dev.hilla.parser.core;

import javax.annotation.Nonnull;

import dev.hilla.parser.models.NamedModel;
import org.slf4j.LoggerFactory;

public interface Processor {
    @Nonnull
    ScanLocation traverse(ScanLocation location);

    void process(ScanLocation location, SharedStorage sharedStorage);
}
