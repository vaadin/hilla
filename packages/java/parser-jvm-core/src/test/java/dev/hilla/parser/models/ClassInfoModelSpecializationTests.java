package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.SpecializedModel;
import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodTypeSignature;

@ExtendWith(ParserExtension.class)
public class ClassInfoModelSpecializationTests {
    private static final Map<String, String[]> specializations = Map.ofEntries(
            entry(Boolean.class.getName(), "isJDKClass", "isBoolean"),
            entry(Byte.class.getName(), "isJDKClass", "isByte",
                    "hasIntegerType"),
            entry(Character.class.getName(), "isJDKClass", "isCharacter"),
            entry(Double.class.getName(), "isJDKClass", "isDouble",
                    "hasFloatType"),
            entry(Float.class.getName(), "isJDKClass", "isFloat",
                    "hasFloatType"),
            entry(Integer.class.getName(), "isJDKClass", "isInteger",
                    "hasIntegerType"),
            entry(Long.class.getName(), "isJDKClass", "isLong",
                    "hasIntegerType"),
            entry(Short.class.getName(), "isJDKClass", "isShort",
                    "hasIntegerType"),
            entry(Void.class.getName(), "isJDKClass"));

    private final SpecializationChecker<SpecializedModel> checker = new SpecializationChecker<>(
            SpecializedModel.class,
            SpecializedModel.class.getDeclaredMethods());

    @DisplayName("It should detect class specialization correctly")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_DetectClassSpecialization(ClassInfoModel model,
            Object origin, String[] specializations, ModelKind kind,
            ModelProvider.Context context, String testName) {
        checker.apply(model, specializations);
    }

    public static final class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static final class Context extends BaseTestContext {
            Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return Arrays.stream(Sample.class.getDeclaredMethods())
                        .map(Method::getReturnType)
                        .map(origin -> Arguments.of(ClassInfoModel.of(origin),
                                origin, specializations.get(origin.getName()),
                                ModelKind.REFLECTION, this,
                                origin.getSimpleName()));
            }

            public Stream<Arguments> getSourceArguments() {
                return getScanResult().getClassInfo(Sample.class.getName())
                        .getMethodInfo().stream()
                        .map(MethodInfo::getTypeSignatureOrTypeDescriptor)
                        .map(MethodTypeSignature::getResultType)
                        .map(ClassRefTypeSignature.class::cast)
                        .map(ClassRefTypeSignature::getClassInfo)
                        .map(origin -> Arguments.of(ClassInfoModel.of(origin),
                                origin, specializations.get(origin.getName()),
                                ModelKind.SOURCE, this,
                                origin.getSimpleName()));
            }
        }
    }

    private static class Sample {
        public Boolean getBoolean() {
            return true;
        }

        public Byte getByte() {
            return 0;
        }

        public Character getCharacter() {
            return 'a';
        }

        public Double getDouble() {
            return 0.0;
        }

        public Float getFloat() {
            return 0.0f;
        }

        public Integer getInteger() {
            return 0;
        }

        public Long getLong() {
            return 0L;
        }

        public Short getShort() {
            return 0;
        }

        public Void getVoid() {
            return null;
        }
    }
}
