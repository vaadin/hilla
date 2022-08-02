package dev.hilla.parser.plugins.nonnull.nonnullapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Endpoint
public class NonNullApiEndpoint {
    public String hello(String hello) {
        return "Hello " + hello;
    }

    @Nullable
    public String helloNullable(@Nullable String hello) {
        return null;
    }

    public Map<List<String>, Set<Map<Integer, String>>> helloNestedTypes(
            Map<Integer, List<String>> param) {
        return new HashMap<>();
    }
}
