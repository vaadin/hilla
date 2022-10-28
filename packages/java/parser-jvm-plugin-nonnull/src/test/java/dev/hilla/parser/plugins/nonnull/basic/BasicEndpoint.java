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

    @dev.hilla.shared.Nonnull
    public List<@Nonnull String> mixedAnnotations(@Nonnull String parameter) {
        return List.of(parameter);
    }

    @dev.hilla.shared.Nonnull
    public String nonTypeAnnotation(
            @dev.hilla.shared.Nonnull String nonTypeParameter) {
        return nonTypeParameter;
    }

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

    public static class ExtendedNonNullableModel extends NonNullableModel {
        @dev.hilla.shared.Nonnull
        public List<@Nonnull String> mixedAnnotations;

        @dev.hilla.shared.Nonnull
        private String nonTypeAnnotation;
    }

    public static class NonNullableModel {
        @Nonnull
        public String publicField;

        @Nonnull
        protected String protectedField;

        @Nonnull
        private Map<String, @Nonnull List<@Nonnull NonNullableModel>> complexTypeField;

        private String nullableField;

        private List<@Nonnull String> typeWithTypeArgument;
    }
}
