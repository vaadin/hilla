package dev.hilla.parser.plugins.nonnull.basic;

import java.util.List;
import java.util.Map;

@Endpoint
public class BasicEndpoint {
    @Nonnull
    public Map<String, @Nonnull List<@Nonnull ExtendedNonNullableModel>> complexType(
            @Nonnull Map<String, @Nonnull List<@Nonnull ExtendedNonNullableModel>> map) {
        return map;
    }

    @javax.annotation.Nonnull
    public List<@Nonnull String> mixedAnnotations(@Nonnull String parameter) {
        return List.of(parameter);
    }

    @javax.annotation.Nonnull
    public String nonTypeAnnotation(
            @javax.annotation.Nonnull String nonTypeParameter) {
        return nonTypeParameter;
    }

    public String nullableType(String nullableParameter) {
        return nullableParameter;
    }

    @Nonnull
    public String simpleType(@Nonnull String str) {
        return str;
    }

    public List<@Nonnull ? extends String> typeArgumentWildcard(
            List<@Nonnull ? extends String> list) {
        return list;
    }

    public <@Nonnull T extends String> List<T> typeParameter(List<T> list) {
        return list;
    }

    public List<@Nonnull NonNullableModel> typeWithTypeArgument(
            List<@Nonnull NonNullableModel> list) {
        return list;
    }

    public static class ExtendedNonNullableModel extends NonNullableModel {
        @javax.annotation.Nonnull
        public List<@Nonnull String> mixedAnnotations;

        @javax.annotation.Nonnull
        public String nonTypeAnnotation;
    }

    public static class NonNullableModel {
        @Nonnull
        public Map<String, @Nonnull List<@Nonnull NonNullableModel>> complexTypeField;
        public String nullableField;
        @Nonnull
        public String protectedField;
        @Nonnull
        public String publicField;
        public List<@Nonnull String> typeWithTypeArgument;
    }
}
