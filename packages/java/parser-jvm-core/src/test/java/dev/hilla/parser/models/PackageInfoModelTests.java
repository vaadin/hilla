package dev.hilla.parser.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.models.pack.Foo;
import dev.hilla.parser.models.pack.PackageInfoModelSample;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.Source;
import dev.hilla.parser.test.helpers.SourceExtension;

import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;

@ExtendWith(SourceExtension.class)
public class PackageInfoModelTests {
    private Context ctx;

    @BeforeEach
    public void setUp(@Source ScanResult source) {
        this.ctx = new Context(source);
    }

    @DisplayName("It should contain an annotation")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ContainAnnotation(PackageInfoModel model,
            ModelKind kind) {
        assertEquals(List.of(Foo.class.getName()),
                model.getAnnotationsStream().map(AnnotationInfoModel::getName)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should get annotations with ClassInfo")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    @Disabled("Probably, ClassGraph issue: ScanResult is null for package-level annotations for now")
    public void should_GetAnnotationsWithClassInfo(PackageInfoModel model,
            ModelKind kind) {
        assertEquals(List.of(ClassInfoModel.of(Foo.class)),
                model.getAnnotationsStream()
                        .map(AnnotationInfoModel::getClassInfo)
                        .collect(Collectors.toList()));
    }

    @DisplayName("It should have the same hashCode for source and reflection models")
    @Test
    public void should_HaveSameHashCodeForSourceAndReflectionModels() {
        var reflectionModel = PackageInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = PackageInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel.hashCode(), sourceModel.hashCode());
    }

    @DisplayName("It should have source and reflection models equal")
    @Test
    public void should_HaveSourceAndReflectionModelsEqual() {
        var reflectionModel = PackageInfoModel.of(ctx.getReflectionOrigin());
        var sourceModel = PackageInfoModel.of(ctx.getSourceOrigin());

        assertEquals(reflectionModel, reflectionModel);
        assertEquals(reflectionModel, sourceModel);

        assertEquals(sourceModel, sourceModel);
        assertEquals(sourceModel, reflectionModel);

        assertNotEquals(sourceModel, new Object());
        assertNotEquals(reflectionModel, new Object());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(PackageInfoModel model,
            ModelKind kind) {
        switch (kind) {
        case REFLECTION:
            assertEquals(ctx.getReflectionOrigin(), model.get());
            break;
        case SOURCE:
            assertEquals(ctx.getSourceOrigin(), model.get());
            break;
        }
    }

    static class Context {
        private static final Package reflectionOrigin = PackageInfoModelSample.class
                .getPackage();

        private final ScanResult source;
        private final PackageInfo sourceOrigin;

        Context(ExtensionContext context) {
            this(SourceExtension.getSource(context));
        }

        Context(ScanResult source) {
            this.source = source;
            this.sourceOrigin = source
                    .getClassInfo(PackageInfoModelSample.class.getName())
                    .getPackageInfo();
        }

        public Package getReflectionOrigin() {
            return reflectionOrigin;
        }

        public ScanResult getSource() {
            return source;
        }

        public PackageInfo getSourceOrigin() {
            return sourceOrigin;
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        static final String testNamePattern = "{1}";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Stream.of(
                    Arguments.of(PackageInfoModel.of(ctx.getReflectionOrigin()),
                            ModelKind.REFLECTION),
                    Arguments.of(PackageInfoModel.of(ctx.getSourceOrigin()),
                            ModelKind.SOURCE));
        }
    }
}
