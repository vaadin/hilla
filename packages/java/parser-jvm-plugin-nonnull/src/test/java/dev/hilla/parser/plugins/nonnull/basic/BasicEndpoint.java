package dev.hilla.parser.plugins.nonnull.basic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Nonnull
    public Optional<@Nonnull String> optional(
            @Nonnull Optional<@Nonnull String> opt) {
        return opt;
    }

    @Nonnull
    public NonNullableFieldModel nonNullableFieldModel(
            @Nonnull NonNullableFieldModel nonNullableFieldModel) {
        return nonNullableFieldModel;
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

    public static class NonNullableFieldModel {
        @Nonnull
        private List<@Nonnull String> stringList;

        public List<String> getStringList() {
            return this.stringList;
        }

        public void setStringList(List<String> stringList) {
            this.stringList = stringList;
        }
    }
}
