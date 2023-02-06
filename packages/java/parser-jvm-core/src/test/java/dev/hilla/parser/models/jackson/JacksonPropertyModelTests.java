package dev.hilla.parser.models.jackson;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getAnyDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toGetterName;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toSetterName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

public class JacksonPropertyModelTests {
    private JacksonPropertyShared.Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new JacksonPropertyShared.Context();
    }

    @DisplayName("It should have correct type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_HaveCorrectType(JacksonPropertyModel model,
            String name) {
        assertTrue(model.getType().getPrimary().get().toString()
                .endsWith(JacksonPropertyShared.stringifiedProps.get(name)));
    }

    @DisplayName("It should pass equality check")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_PassEqualityCheck(JacksonPropertyModel model,
            String name) {
        assertEquals(model, model);
        assertEquals(JacksonPropertyModel.of(ctx.getReflectionOrigin(name)),
                model);
        assertNotEquals(model, new Object());
    }

    @DisplayName("It should correctly process specific Jackson methods")
    @Test
    public void should_ProcessSpecificJacksonMethods() {
        var beanPropertyDefinitionMock = mock(BeanPropertyDefinition.class);
        when(beanPropertyDefinitionMock.couldDeserialize()).thenReturn(true);
        when(beanPropertyDefinitionMock.isExplicitlyIncluded())
                .thenReturn(true);

        var model = JacksonPropertyModel.of(beanPropertyDefinitionMock);

        assertTrue(model.couldDeserialize());
        verify(beanPropertyDefinitionMock, times(1)).couldDeserialize();

        assertTrue(model.isExplicitlyIncluded());
        verify(beanPropertyDefinitionMock, times(1)).isExplicitlyIncluded();
    }

    @DisplayName("It should provide common model class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCommonModelClass(JacksonPropertyModel model,
            String name) {
        assertEquals(model.getCommonModelClass(), JacksonPropertyModel.class);
    }

    @DisplayName("It should provide correct accessor")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectAccessor(JacksonPropertyModel model,
            String name) {
        var expected = switch (name) {
        case "publicProperty", "renamedPublicProperty" -> model.getField();
        case "propertySetterOnly" -> Optional.empty();
        default -> model.getGetter();
        };

        assertEquals(expected, model.getAccessor());
    }

    @DisplayName("It should provide correct mutator")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectMutator(JacksonPropertyModel model,
            String name) {
        var expected = switch (name) {
        case "privatePropertyWithAccessors", "propertySetterOnly" -> model
                .getSetter();
        case "propertyGetterOnly" -> Optional.empty();
        default -> model.getField();
        };

        assertEquals(expected, model.getMutator());
    }

    @DisplayName("It should provide correct origin")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectOrigin(JacksonPropertyModel model,
            String name) {
        assertEquals(ctx.getReflectionOrigin(name), model.get());
        assertTrue(model.isReflection());
    }

    @DisplayName("It should provide correct property info")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectPropertyInfo(JacksonPropertyModel model,
            String name) {
        var n = switch (name) {
        case "renamedPublicProperty", "renamedPrivateProperty" -> name + "0";
        default -> name;
        };
        assertEquals(n, model.getName());
        assertEquals(ClassInfoModel.of(JacksonPropertyShared.Sample.class),
                model.getOwner());
    }

    @DisplayName("It should provide property annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyAnnotations(JacksonPropertyModel model,
            String name) {
        var expected = switch (name) {
        case "privatePropertyWithAccessors" -> Set.of(
                "dev.hilla.parser.models.jackson.JacksonPropertyShared$FieldAnnotation",
                "dev.hilla.parser.models.jackson.JacksonPropertyShared$MethodAnnotation",
                "dev.hilla.parser.models.jackson.JacksonPropertyShared$ParameterAnnotation");
        case "renamedPublicProperty", "renamedPrivateProperty" -> Set
                .of("com.fasterxml.jackson.annotation.JsonProperty");
        default -> Set.of();
        };

        assertEquals(expected, model.getAnnotations().stream()
                .map(a -> ((Class<?>) a.getClassInfo().get().get()).getName())
                .collect(Collectors.toSet()));
    }

    @DisplayName("It should provide property field (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyField(JacksonPropertyModel model,
            String name) {
        record Expected(boolean hasField, Optional<FieldInfoModel> field) {
        }

        var expected = switch (name) {
        case "propertyGetterOnly", "propertySetterOnly" -> new Expected(false,
                Optional.empty());
        default -> new Expected(true, Optional.of(FieldInfoModel
                .of(getDeclaredField(JacksonPropertyShared.Sample.class, name))));
        };

        assertEquals(expected.hasField(), model.hasField());
        assertEquals(expected.field(), model.getField());
    }

    @DisplayName("It should provide property getter (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyGetter(JacksonPropertyModel model,
            String name) {
        record Expected(boolean hasGetter, Optional<MethodInfoModel> getter) {
        }

        var expected = switch (name) {
        case "publicProperty", "renamedPublicProperty", "propertySetterOnly" -> new Expected(
                false, Optional.empty());
        default -> new Expected(true,
                Optional.of(MethodInfoModel.of(getDeclaredMethod(
                        JacksonPropertyShared.Sample.class, toGetterName(name)))));
        };

        assertEquals(expected.hasGetter(), model.hasGetter());
        assertEquals(expected.getter(), model.getGetter());
    }

    @DisplayName("It should provide property setter (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertySetter(JacksonPropertyModel model,
            String name) {
        record Expected(boolean hasSetter, Optional<MethodInfoModel> setter) {
        }

        var expected = switch (name) {
        case "privatePropertyWithAccessors", "propertySetterOnly" -> new Expected(
                true, getAnyDeclaredMethod(JacksonPropertyShared.Sample.class,
                        toSetterName(name)).map(MethodInfoModel::of));
        default -> new Expected(false, Optional.empty());
        };

        assertEquals(expected.hasSetter(), model.hasSetter());
        assertEquals(expected.setter(), model.getSetter());
    }

    @DisplayName("It should provide the same hash code for same properties")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideSameHashCodeForSameProperties(
            JacksonPropertyModel model, String name) {
        assertEquals(JacksonPropertyModel.of(ctx.getReflectionOrigin(name))
                .hashCode(), model.hashCode());
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new JacksonPropertyShared.Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            JacksonPropertyModel.of(entry.getValue()),
                            entry.getKey()));
        }
    }

}
