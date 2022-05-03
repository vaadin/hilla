package dev.hilla.parser.test.helpers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecializationChecker<Model> {
    private final Map<String, Function<Model, Boolean>> functions = new HashMap<>();

    public SpecializationChecker(Class<Model> modelClass, Method... methods) {
        this(modelClass, Arrays.asList(methods));
    }

    public SpecializationChecker(Class<Model> modelClass,
            Stream<Method> methods) {
        this(modelClass, methods.collect(Collectors.toList()));
    }

    public SpecializationChecker(Class<Model> modelClass,
            List<Method> methods) {
        try {
            var lookup = MethodHandles.lookup();

            for (var method : methods) {
                var site = LambdaMetafactory.metafactory(lookup, "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        lookup.unreflect(method),
                        MethodType.methodType(Boolean.class, modelClass));

                var function = (Function<Model, Boolean>) site.getTarget()
                        .invokeExact();

                functions.put(method.getName(), function);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public static <K, V> Map.Entry<K, V[]> entry(K key, V... values) {
        return Map.entry(key, values);
    }

    public void apply(Model model, String... expected) {
        apply(model, Arrays.asList(expected));
    }

    public void apply(Model model, List<String> expected) {
        assertAll(functions.entrySet().stream().map(entry -> {
            var name = entry.getKey();
            var value = entry.getValue().apply(model);

            if (expected.contains(name)) {
                return () -> assertTrue(value,
                        String.format("'%s' expected to return true", name));
            }

            return () -> assertFalse(value,
                    String.format("'%s' expected to return false", name));
        }));
    }
}
