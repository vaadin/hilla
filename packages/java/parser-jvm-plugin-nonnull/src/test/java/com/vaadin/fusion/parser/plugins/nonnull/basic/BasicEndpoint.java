package com.vaadin.fusion.parser.plugins.nonnull.basic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@Endpoint
public class BasicEndpoint {
    public String nullableType(String nullableParameter) {
        return nullableParameter;
    }

    @Nonnull
    public String simpleType(@Nonnull String str) {
        return str;
    }

    public List<@Nonnull NonNullableModel> typeWithTypeArgument(
            List<@Nonnull NonNullableModel> list) {
        return list;
    }

    @Nonnull
    public Map<String, @Nonnull List<@Nonnull ExtendedNonNullableModel>> complexType(
            @Nonnull Map<String, @Nonnull List<@Nonnull ExtendedNonNullableModel>> map) {
        return map;
    }

    @javax.annotation.Nonnull
    public String nonTypeAnnotation(
            @javax.annotation.Nonnull String nonTypeParameter) {
        return nonTypeParameter;
    }

    @javax.annotation.Nonnull
    public List<@Nonnull String> mixedAnnotations(@Nonnull String parameter) {
        return List.of(parameter);
    }

    public static class NonNullableModel {
        public String getNullableType(String nullableParameter) {
            return nullableParameter;
        }

        @Nonnull
        public String getSimpleType(@Nonnull String str) {
            return str;
        }

        public List<@Nonnull String> getTypeWithTypeArgument(
                List<@Nonnull String> list) {
            return list;
        }

        @Nonnull
        public Map<String, @Nonnull List<@Nonnull NonNullableModel>> getComplexType(
                @Nonnull Map<String, @Nonnull List<@Nonnull NonNullableModel>> map) {
            return map;
        }
    }

    public static class ExtendedNonNullableModel extends NonNullableModel {
        @javax.annotation.Nonnull
        public String getNonTypeAnnotation(
                @javax.annotation.Nonnull String nonTypeParameter) {
            return nonTypeParameter;
        }

        @javax.annotation.Nonnull
        public List<@Nonnull String> getMixedAnnotations(
                @Nonnull String parameter) {
            return List.of(parameter);
        }
    }
}
