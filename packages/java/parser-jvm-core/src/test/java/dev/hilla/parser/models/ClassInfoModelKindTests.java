package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.SpecializationChecker.entry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
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
import dev.hilla.parser.test.helpers.BaseTestContext;
import dev.hilla.parser.test.helpers.ModelKind;
import dev.hilla.parser.test.helpers.ParserExtension;
import dev.hilla.parser.test.helpers.SpecializationChecker;
import dev.hilla.parser.utils.Streams;

import io.github.classgraph.ArrayTypeSignature;

@ExtendWith(ParserExtension.class)
public class ClassInfoModelKindTests {
    private static final Map<Class<?>, String[]> associations = Map.ofEntries(
            entry(Kind.Abstract.class, "isAbstract", "isStandardClass",
                    "isStatic"),
            entry(Kind.Annotation.class, "isAnnotation", "isAbstract",
                    "isInterfaceOrAnnotation", "isStatic"),
            entry(Object[].class, "isAbstract", "isArrayClass", "isFinal",
                    "isPublic", "isStandardClass"),
            entry(Kind.Enum.class, "isEnum", "isFinal", "isStandardClass",
                    "isStatic"),
            entry(Kind.Final.class, "isFinal", "isStandardClass", "isStatic"),
            entry(Kind.Interface.class, "isAbstract", "isInterface",
                    "isInterfaceOrAnnotation", "isStatic"),
            entry(Byte.class, "isFinal", "isPublic", "isStandardClass"),
            entry(Kind.Private.class, "isPrivate", "isStandardClass",
                    "isStatic"),
            entry(Kind.Protected.class, "isProtected", "isStandardClass",
                    "isStatic"),
            entry(Kind.Public.class, "isPublic", "isStandardClass",
                    "isStatic"));

    private final KindChecker checker = new KindChecker();

    @DisplayName("It should detect class kind correctly")
    @ParameterizedTest(name = ModelProvider.testName)
    @ArgumentsSource(ModelProvider.class)
    public void should_DetectClassKind(ClassInfoModel model, Object origin,
            String[] specializations, ModelKind kind,
            ModelProvider.Context context, String testName) {
        checker.apply(model, specializations);
    }

    private static final class Kind {
        enum Enum {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Annotation {
        }

        interface Interface {
        }

        static final class Final {
        }

        public static class Public {
        }

        protected static class Protected {
        }

        static abstract class Abstract {
        }

        private static class Private {
        }
    }

    private static final class KindChecker
            extends SpecializationChecker<ClassInfoModel> {
        private static final List<String> allowedMethods = List.of("isAbstract",
                "isAnnotation", "isArrayClass", "isEnum", "isFinal",
                "isInterface", "isInterfaceOrAnnotation", "isNative",
                "isPrivate", "isProtected", "isPublic", "isStandardClass",
                "isStatic", "isSynthetic");

        public KindChecker() {
            super(ClassInfoModel.class,
                    Arrays.stream(ClassInfoModel.class.getDeclaredMethods())
                            .filter(method -> allowedMethods
                                    .contains(method.getName())));
        }
    }

    public static class ModelProvider implements ArgumentsProvider {
        public static final String testName = "{3} [{5}]";

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext context) {
            var ctx = new Context(context);

            return Streams.combine(ctx.getReflectionArguments(),
                    ctx.getSourceArguments());
        }

        public static class Context extends BaseTestContext {
            public Context(ExtensionContext context) {
                super(context);
            }

            public Stream<Arguments> getReflectionArguments() {
                return associations.entrySet().stream().map(entry -> {
                    var origin = entry.getKey();
                    var model = ClassInfoModel.of(origin);
                    var testName = origin.getSimpleName();

                    return Arguments.of(model, origin, entry.getValue(),
                            ModelKind.REFLECTION, this, testName);
                });
            }

            public Stream<Arguments> getSourceArguments() {
                return associations.entrySet().stream().map(entry -> {
                    var cls = entry.getKey();

                    var origin = cls.isArray()
                            ? ((ArrayTypeSignature) getScanResult()
                                    .getClassInfo(UnsearchableTypesSample.class
                                            .getName())
                                    .getFieldInfo("array")
                                    .getTypeSignatureOrTypeDescriptor())
                                            .getArrayClassInfo()
                            : getScanResult().getClassInfo(cls.getName());
                    var model = ClassInfoModel.of(origin);
                    var testName = origin.getSimpleName();

                    return Arguments.of(model, origin, entry.getValue(),
                            ModelKind.SOURCE, this, testName);
                });
            }
        }

        private static class UnsearchableTypesSample {
            Object[] array;
        }
    }

}
