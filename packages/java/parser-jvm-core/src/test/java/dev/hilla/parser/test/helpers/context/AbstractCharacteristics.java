package dev.hilla.parser.test.helpers.context;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import io.github.classgraph.ScanResult;

public abstract class AbstractCharacteristics<ReflectionOrigin extends AnnotatedElement, SourceOrigin>
        extends BaseContext {
    private final Map<ReflectionOrigin, String[]> reflectionCharacteristics;
    private final Map<SourceOrigin, String[]> sourceCharacteristics;

    public AbstractCharacteristics(ScanResult source,
            Map<ReflectionOrigin, String[]> reflectionCharacteristics,
            Map<SourceOrigin, String[]> sourceCharacteristics) {
        super(source);
        this.reflectionCharacteristics = reflectionCharacteristics;
        this.sourceCharacteristics = sourceCharacteristics;
    }

    public Map<ReflectionOrigin, String[]> getReflectionCharacteristics() {
        return reflectionCharacteristics;
    }

    public String[] getReflectionCharacteristicsPerOrigin(
            ReflectionOrigin origin) {
        return reflectionCharacteristics.get(origin);
    }

    public Map<SourceOrigin, String[]> getSourceCharacteristics() {
        return sourceCharacteristics;
    }

    public String[] getSourceCharacteristicsPerOrigin(SourceOrigin origin) {
        return sourceCharacteristics.get(origin);
    }
}
