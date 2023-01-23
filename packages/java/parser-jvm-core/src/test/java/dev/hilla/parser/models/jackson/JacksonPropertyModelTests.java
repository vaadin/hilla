package dev.hilla.parser.models.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import dev.hilla.parser.models.AnnotationInfoModel;
import dev.hilla.parser.models.ClassInfoModel;
import dev.hilla.parser.models.FieldInfoModel;
import dev.hilla.parser.models.MethodInfoModel;
import dev.hilla.parser.models.SignatureModel;

public class JacksonPropertyModelTests {
    Context ctx;

    @BeforeEach
    public void setUp() {
        ctx = new Context();
    }

    @DisplayName("It should have correct type")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_HaveCorrectType(JacksonPropertyModel model,
            String name) {
        assertEquals(ctx.getType(name), model.getType());
    }

    @DisplayName("It should pass equality check")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_PassEqualityCheck(JacksonPropertyModel model,
            String name) {
        var expected = JacksonPropertyModel.of(ctx.getReflectionOrigin(name));

        assertEquals(model, model);
        assertEquals(expected, model);
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
        assertEquals(ctx.getNameAfterRenaming(name), model.getName());
        assertEquals(ClassInfoModel.of(Sample.class), model.getOwner());
    }

    @DisplayName("It should provide property annotations")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyAnnotations(JacksonPropertyModel model,
            String name) {
        assertEquals(ctx.getAnnotations(name), model.getAnnotations());
    }

    @DisplayName("It should provide property field (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyField(JacksonPropertyModel model,
            String name) {
        assertEquals(ctx.getField(name).isPresent(), model.hasField());
        assertEquals(ctx.getField(name), model.getField());
    }

    @DisplayName("It should provide property getter (if there is one)")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvidePropertyGetter(JacksonPropertyModel model,
            String name) {
        assertEquals(ctx.getGetter(name).isPresent(), model.hasGetter());
        assertEquals(ctx.getGetter(name), model.getGetter());
    }

    @DisplayName("It should provide the same hash code for same properties")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideSameHashCodeForSameProperties(
            JacksonPropertyModel model, String name) {
        var expected = JacksonPropertyModel.of(ctx.getReflectionOrigin(name));

        assertEquals(expected.hashCode(), model.hashCode());
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

        public List<AnnotationInfoModel> getAnnotations(String name) {
            return Arrays
                    .stream(reflectionOrigins.get(name).getAccessor()
                            .getAnnotated().getAnnotations())
                    .map(AnnotationInfoModel::of).toList();
        }

        public Optional<FieldInfoModel> getField(String name) {
            return Optional.ofNullable(reflectionOrigins.get(name).getField())
                    .map(AnnotatedField::getAnnotated).map(FieldInfoModel::of);
        }

        public Optional<MethodInfoModel> getGetter(String name) {
            return Optional.ofNullable(reflectionOrigins.get(name).getGetter())
                    .map(AnnotatedMethod::getAnnotated)
                    .map(MethodInfoModel::of);
        }

        public String getNameAfterRenaming(String name) {
            return reflectionOrigins.get(name).getName();
        }

        public BeanPropertyDefinition getReflectionOrigin(String name) {
            return reflectionOrigins.get(name);
        }

        public Map<String, BeanPropertyDefinition> getReflectionOrigins() {
            return reflectionOrigins;
        }

        public SignatureModel getType(String name) {
            var accessor = reflectionOrigins.get(name).getAccessor()
                    .getAnnotated();

            return SignatureModel.of(accessor instanceof Method
                    ? ((Method) accessor).getAnnotatedReturnType()
                    : ((Field) accessor).getAnnotatedType());
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
        private Double renamedPrivateProperty;

        public Map<String, Long> getPrivateProperty() {
            return privateProperty;
        }

        public Boolean[] getPropertyWithoutField() {
            return new Boolean[] {};
        }

        @JsonProperty("renamedPrivateProperty0")
        public List<Double> getRenamedPrivateProperty() {
            return List.of(renamedPrivateProperty);
        }
    }
}
