package dev.hilla.parser.test.helpers.context;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import io.github.classgraph.ScanResult;

public abstract class AbstractContext<ReflectionOrigin extends AnnotatedElement, SourceOrigin>
        extends BaseContext {
    private final Map<String, ReflectionOrigin> reflectionOrigins;
    private final Map<String, SourceOrigin> sourceOrigins;

    protected AbstractContext(ScanResult source,
            Map<String, ReflectionOrigin> reflectionOrigins,
            Map<String, SourceOrigin> sourceOrigins) {
        super(source);
        this.reflectionOrigins = reflectionOrigins;
        this.sourceOrigins = sourceOrigins;
    }

    public ReflectionOrigin getReflectionOrigin(String name) {
        return reflectionOrigins.get(name);
    }

    public Map<String, ReflectionOrigin> getReflectionOrigins() {
        return reflectionOrigins;
    }

    public SourceOrigin getSourceOrigin(String name) {
        return sourceOrigins.get(name);
    }

    public Map<String, SourceOrigin> getSourceOrigins() {
        return sourceOrigins;
    }
}
