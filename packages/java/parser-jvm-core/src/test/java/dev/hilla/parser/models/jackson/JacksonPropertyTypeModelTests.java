package dev.hilla.parser.models.jackson;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getAnyDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toGetterName;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toSetterName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.hilla.parser.models.SignatureModel;

public class JacksonPropertyTypeModelTests {
    private JacksonPropertyShared.Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new JacksonPropertyShared.Context();
    }

    @DisplayName("It should get type of the primary member")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_GetTypeOfPrimaryMember(JacksonPropertyTypeModel model,
            String name) {
        assertTrue(model.getPrimary().get().toString()
                .endsWith(JacksonPropertyShared.stringifiedProps.get(name)));
    }

    @DisplayName("It should pass equality check")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_PassEqualityCheck(JacksonPropertyTypeModel model,
            String name) {
        assertEquals(model, model);
        assertEquals(JacksonPropertyTypeModel.of(ctx.getReflectionOrigin(name)),
                model);
        assertNotEquals(model, new Object());
    }

    @DisplayName("It should provide common model class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCommonModelClass(JacksonPropertyTypeModel model,
            String name) {
        assertEquals(model.getCommonModelClass(),
                JacksonPropertyTypeModel.class);
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(JacksonPropertyTypeModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
        assertTrue(model.isReflection());
    }

    @DisplayName("It should provide property field type (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyFieldType(JacksonPropertyTypeModel model,
            String name) {
        record Expected(boolean hasField, Optional<SignatureModel> field) {
        }

        var expected = switch (name) {
        case "propertyGetterOnly", "propertySetterOnly" -> new Expected(false,
                Optional.empty());
        default -> new Expected(true, Optional.of(SignatureModel
                .of(getDeclaredField(JacksonPropertyShared.Sample.class, name)
                        .getAnnotatedType())));
        };

        assertEquals(expected.hasField(), model.hasField());
        assertEquals(expected.field(), model.getField());
    }

    @DisplayName("It should provide property getter type (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyGetterType(JacksonPropertyTypeModel model,
            String name) {
        record Expected(boolean hasGetter, Optional<SignatureModel> getter) {
        }

        var expected = switch (name) {
        case "publicProperty", "renamedPublicProperty", "propertySetterOnly" -> new Expected(
                false, Optional.empty());
        default -> new Expected(true,
                Optional.of(SignatureModel.of(
                        getDeclaredMethod(JacksonPropertyShared.Sample.class,
                                toGetterName(name)).getAnnotatedReturnType())));
        };

        assertEquals(expected.hasGetter(), model.hasGetter());
        assertEquals(expected.getter(), model.getGetter());
    }

    @DisplayName("It should provide property setter type (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertySetterType(JacksonPropertyTypeModel model,
            String name) {
        record Expected(boolean hasSetter, Optional<SignatureModel> setter) {
        }

        var expected = switch (name) {
        case "privatePropertyWithAccessors", "propertySetterOnly" -> new Expected(
                true,
                getAnyDeclaredMethod(JacksonPropertyShared.Sample.class,
                        toSetterName(name)).map(Method::getParameters)
                                .map(List::of).map(l -> l.get(0))
                                .map(Parameter::getAnnotatedType)
                                .map(SignatureModel::of));
        default -> new Expected(false, Optional.empty());
        };

        assertEquals(expected.hasSetter(), model.hasSetter());
        assertEquals(expected.setter(), model.getSetter());
    }

    @DisplayName("It should provide the same hash code for same properties")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideSameHashCodeForSameProperties(
            JacksonPropertyTypeModel model, String name) {
        assertEquals(JacksonPropertyTypeModel.of(ctx.getReflectionOrigin(name))
                .hashCode(), model.hashCode());
    }

    @DisplayName("It should provide correct type annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideTypeAnnotations(JacksonPropertyTypeModel model,
            String name) {
        var expected = "privatePropertyWithAccessors".equals(
                name) ? Set.of("dev.hilla.parser.models.jackson.JacksonPropertyShared$FieldTypeAnnotation", "dev.hilla.parser.models.jackson.JacksonPropertyShared$MethodTypeAnnotation", "dev.hilla.parser.models.jackson.JacksonPropertyShared$ParameterTypeAnnotation") : Set.of();

        assertEquals(expected, model.getAnnotations().stream()
                .map(a -> ((Class<?>) a.getClassInfo().get().get()).getName())
                .collect(Collectors.toSet()));
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new JacksonPropertyShared.Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            JacksonPropertyTypeModel.of(entry.getValue()),
                            entry.getKey()));
        }
    }
}
