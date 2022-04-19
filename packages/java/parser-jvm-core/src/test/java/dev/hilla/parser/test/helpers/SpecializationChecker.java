package dev.hilla.parser.test.helpers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.hilla.parser.models.SpecializedModel;

public class SpecializationChecker {
    private static final Map<String, Function<SpecializedModel, Boolean>> methods = new HashMap<>();

    static {
        try {
            var lookup = MethodHandles.lookup();
            for (var method : SpecializedModel.class.getDeclaredMethods()) {
                var site = LambdaMetafactory.metafactory(lookup, "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        lookup.unreflect(method), MethodType.methodType(
                                Boolean.class, SpecializedModel.class));

                var function = (Function<SpecializedModel, Boolean>) site
                        .getTarget().invokeExact();

                methods.put(method.getName(), function);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<String, Boolean> results;

    public SpecializationChecker(SpecializedModel model) {
        results = methods.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().apply(model)));
    }

    public void apply(String... expected) {
        apply(Arrays.asList(expected));
    }

    public void apply(List<String> expected) {
        results.forEach((name, value) -> {
            if (expected.contains(name)) {
                assertTrue(value,
                        String.format("'%s' expected to return true", name));
            } else {
                assertFalse(value,
                        String.format("'%s' expected to return false", name));
            }
        });
    }
}
