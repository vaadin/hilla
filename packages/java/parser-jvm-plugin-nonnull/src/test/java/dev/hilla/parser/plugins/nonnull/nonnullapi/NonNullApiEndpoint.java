package dev.hilla.parser.plugins.nonnull.nonnullapi;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Endpoint
public class NonNullApiEndpoint {
    public Dependency defaultMethod(String param) {
        return null;
    }

    public Map<String, Set<Dependency>> nestedSignatureMethod(
            Set<Dependency> param) {
        return null;
    }

    @NullableMethod
    public Dependency nullableMethod(@NullableParameter String param) {
        return null;
    }

    public Map<String, @NullableSignature Set<@NullableSignature Dependency>> nullableNestedSignatureMethod(
            Set<@NullableSignature Dependency> param) {
        return null;
    }

    @NullableSignature
    public Dependency nullableSignature(@NullableSignature String param) {
        return null;
    }

    static class Dependency {
        public String defaultField;
        @NullableField
        public String nullableField;
        @NullableSignature
        public String nullableSignatureField;
    }

    public Optional<String> optionalMethod(Optional<String> opt) {
        return opt;
    }
}
