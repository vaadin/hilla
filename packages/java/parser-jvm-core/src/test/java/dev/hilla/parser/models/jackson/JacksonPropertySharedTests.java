package dev.hilla.parser.models.jackson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

final class JacksonPropertySharedTests {

    static final Map<String, List<String>> stringifiedTypes = Map.ofEntries(
            Map.entry("privatePropertyWithAccessors", List.of(
                    "@dev.hilla.parser.models.jackson.JacksonPropertySharedTests$MethodTypeAnnotation() java.lang.Byte",
                    "@dev.hilla.parser.models.jackson.JacksonPropertySharedTests$ParameterTypeAnnotation() java.lang.Byte",
                    "@dev.hilla.parser.models.jackson.JacksonPropertySharedTests$FieldTypeAnnotation() java.lang.Byte")),
            Map.entry("privateProperty",
                    List.of("java.util.Map<java.lang.String, java.lang.Long>",
                            "java.util.Map<java.lang.String, java.lang.Long>")),
            Map.entry("renamedPublicProperty", List.of("java.lang.Float")),
            Map.entry("renamedPrivateProperty",
                    List.of("java.util.List<java.lang.Double>",
                            "java.lang.Double")),
            Map.entry("propertyWithoutAccessor", List.of("java.lang.Short")),
            Map.entry("publicProperty",
                    List.of("java.util.List<java.lang.Integer>")),
            Map.entry("propertyGetterOnly", List.of("java.lang.Boolean[]")),
            Map.entry("propertySetterOnly", List.of("java.lang.Short")));

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface FieldAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface FieldTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MethodAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface MethodTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface ParameterAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface ParameterTypeAnnotation {
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

    static class Sample {
        public List<Integer> publicProperty;
        @JsonProperty("renamedPublicProperty0")
        public Float renamedPublicProperty;
        private Map<String, Long> privateProperty;
        @FieldTypeAnnotation
        @FieldAnnotation
        private Byte privatePropertyWithAccessors;
        private Double renamedPrivateProperty;

        public Map<String, Long> getPrivateProperty() {
            return privateProperty;
        }

        @MethodAnnotation
        @MethodTypeAnnotation
        public Byte getPrivatePropertyWithAccessors() {
            return privatePropertyWithAccessors;
        }

        public void setPrivatePropertyWithAccessors(
                @ParameterAnnotation @ParameterTypeAnnotation Byte privatePropertyWithAccessors) {
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
