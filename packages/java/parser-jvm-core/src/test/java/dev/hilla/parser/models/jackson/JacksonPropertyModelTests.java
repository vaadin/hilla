package dev.hilla.parser.models.jackson;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getAnyDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toGetterName;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.toSetterName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;

public class JacksonPropertyModelTests {
    private static final Map<String, String> stringifiedProps = Map.ofEntries(
            Map.entry("privatePropertyWithAccessors", "java.lang.Byte"),
            Map.entry("privateProperty",
                    "java.util.Map<java.lang.String, java.lang.Long>"),
            Map.entry("renamedPublicProperty", "java.lang.Float"),
            Map.entry("renamedPrivateProperty",
                    "java.util.List<java.lang.Double>"),
            Map.entry("propertyWithoutAccessor", "java.lang.Short"),
            Map.entry("publicProperty", "java.util.List<java.lang.Integer>"),
            Map.entry("propertyGetterOnly", "java.lang.Boolean[]"),
            Map.entry("propertySetterOnly", "java.lang.Short"));

    private Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context();
    }

    @DisplayName("It should have correct type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_HaveCorrectType(JacksonPropertyModel model,
            String name) {
        assertEquals(stringifiedProps.get(name),
                model.getType().getPrimary().get().toString());
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

    @DisplayName("It should provide common model class")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCommonModelClass(JacksonPropertyModel model,
            String name) {
        assertEquals(model.getCommonModelClass(), JacksonPropertyModel.class);
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
        assertEquals(ClassInfoModel.of(Sample.class), model.getOwner());
    }

    @DisplayName("It should provide property annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyAnnotations(JacksonPropertyModel model,
            String name) {
        var expected = switch (name) {
        case "renamedPublicProperty" -> Map.ofEntries(
                Map.entry("com.fasterxml.jackson.annotation.JsonProperty",
                        "renamedPublicProperty0"));
        case "renamedPrivateProperty" -> Map.ofEntries(
                Map.entry("com.fasterxml.jackson.annotation.JsonProperty",
                        "renamedPrivateProperty0"));
        default -> Map.ofEntries();
        };

        assertEquals(expected,
                model.getAnnotations().stream().collect(Collectors.toMap(
                        a -> ((Class<?>) a.getClassInfo().get().get())
                                .getName(),
                        a -> a.getParameters().stream()
                                .filter(p -> "value".equals(p.getName()))
                                .findFirst().get().getValue())));
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
        default -> new Expected(true, Optional
                .of(FieldInfoModel.of(getDeclaredField(Sample.class, name))));
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
        default -> new Expected(true, Optional.of(MethodInfoModel
                .of(getDeclaredMethod(Sample.class, toGetterName(name)))));
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
                true, getAnyDeclaredMethod(Sample.class, toSetterName(name))
                        .map(MethodInfoModel::of));
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

    static class Context {
        private static final Map<String, BeanPropertyDefinition> reflectionOrigins;

        static {
            var mapper = new ObjectMapper();
            reflectionOrigins = mapper.getSerializationConfig()
                    .introspect(mapper.constructType(Sample.class))
                    .findProperties().stream()
                    .collect(Collectors.toMap(
                            BeanPropertyDefinition::getInternalName,
                            Function.identity()));
        }

        public BeanPropertyDefinition getReflectionOrigin(String name) {
            return reflectionOrigins.get(name);
        }

        public Map<String, BeanPropertyDefinition> getReflectionOrigins() {
            return reflectionOrigins;
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            JacksonPropertyModel.of(entry.getValue()),
                            entry.getKey()));
        }
    }

    static class Sample {
        public List<Integer> publicProperty;
        @JsonProperty("renamedPublicProperty0")
        public Float renamedPublicProperty;
        private Map<String, Long> privateProperty;
        private Byte privatePropertyWithAccessors;
        private Double renamedPrivateProperty;

        public Map<String, Long> getPrivateProperty() {
            return privateProperty;
        }

        public Byte getPrivatePropertyWithAccessors() {
            return privatePropertyWithAccessors;
        }

        public void setPrivatePropertyWithAccessors(
                Byte privatePropertyWithAccessors) {
            this.privatePropertyWithAccessors = privatePropertyWithAccessors;
        }

        public Boolean[] getPropertyGetterOnly() {
            return new Boolean[] {};
        }

        @JsonProperty("renamedPrivateProperty0")
        public List<Double> getRenamedPrivateProperty() {
            return List.of(renamedPrivateProperty);
        }

        public void setPropertySetterOnly(Short s) {
        }
    }
}
