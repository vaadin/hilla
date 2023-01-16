package dev.hilla.parser.models;

import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredField;
import static dev.hilla.parser.test.helpers.ClassMemberUtils.getDeclaredMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class PropertyInfoModelTests {
    @BeforeEach
    public void setUp() {
    }

    @DisplayName("It should provide correct properties")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ExtractPropertiesCorrectly(PropertyInfoModel model,
            PropertyInfo info) {
        assertEquals(info.renamedName(), model.getName());
        assertEquals(ClassInfoModel.of(Sample.class), model.getOwner());
        assertEquals(
                FieldInfoModel.of(getDeclaredField(Sample.class, info.name())),
                model.getField());
        assertEquals(info.hasGetter()
                ? Optional.of(MethodInfoModel
                        .of(getDeclaredMethod(Sample.class, info.getterName())))
                : Optional.empty(), model.getGetter());
    }

    @DisplayName("It should provide correct properties")
    @ParameterizedTest(name = ModelProvider.testNamePattern)
    @ArgumentsSource(ModelProvider.class)
    public void should_ProvideCorrectTypes(PropertyInfoModel model,
            PropertyInfo info) {
        var signature = info.hasGetter()
                ? SignatureModel
                        .of(getDeclaredMethod(Sample.class, info.getterName())
                                .getAnnotatedReturnType())
                : SignatureModel.of(getDeclaredField(Sample.class, info.name())
                        .getAnnotatedType());

        assertEquals(signature, model.getType());
    }

    record PropertyInfo(String name, boolean hasGetter, boolean isRenamed) {
        String renamedName() {
            return isRenamed ? name + '0' : name;
        }

        String getterName() {
            return "get" + Character.toUpperCase(name.charAt(0))
                    + name.substring(1);
        }
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

        public Map<String, BeanPropertyDefinition> getReflectionOrigins() {
            return reflectionOrigins;
        }
    }

    static class ModelProvider implements ArgumentsProvider {
        public static final String testNamePattern = "{1}";
        private static final Pattern noGetterPattern = Pattern
                .compile("public|NoGetter", Pattern.CASE_INSENSITIVE);
        private static final Pattern renamedPattern = Pattern.compile("renamed",
                Pattern.CASE_INSENSITIVE);

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            var ctx = new Context();

            return ctx.getReflectionOrigins().entrySet().stream()
                    .map(entry -> Arguments.of(
                            PropertyInfoModel.of(entry.getValue(),
                                    ClassInfoModel.of(Sample.class)),
                            new PropertyInfo(entry.getKey(),
                                    !noGetterPattern.matcher(entry.getKey())
                                            .find(),
                                    renamedPattern.matcher(entry.getKey())
                                            .find())));
        }
    }

    static class Sample {
        @JsonIgnore
        public Short ignoredPublicProperty;
        public List<Integer> publicProperty;
        @JsonProperty("renamedPublicProperty0")
        public Float renamedPublicProperty;
        private Byte ignoredPrivateProperty;
        private Map<String, Long> privateProperty;
        private Double renamedPrivateProperty;
        private String privatePropertyNoGetter;

        @JsonIgnore
        public Byte getIgnoredPrivateProperty() {
            return ignoredPrivateProperty;
        }

        public Map<String, Long> getPrivateProperty() {
            return privateProperty;
        }

        @JsonProperty("renamedPrivateProperty0")
        public List<Double> getRenamedPrivateProperty() {
            return List.of(renamedPrivateProperty);
        }
    }
}
