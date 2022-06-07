package dev.hilla.parser.test.helpers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;

public class SpecializationChecker<Model> {
    private final Map<String, Function<Model, Boolean>> functions;

    public SpecializationChecker(Class<Model> modelClass, Method... methods) {
        this(modelClass, Arrays.stream(methods));
    }

    public SpecializationChecker(Class<Model> modelClass,
            Stream<Method> methods, Collection<String> allowedMethods) {
        this(modelClass, methods
                .filter(method -> allowedMethods.contains(method.getName())));
    }

    public SpecializationChecker(Class<Model> modelClass,
            Stream<Method> methods) {
        var lookup = MethodHandles.lookup();

        this.functions = methods.collect(Collectors.toMap(Method::getName,
                Failable.asFunction(method -> {
                    var site = LambdaMetafactory.metafactory(lookup, "apply",
                            MethodType.methodType(Function.class),
                            MethodType.methodType(Object.class, Object.class),
                            lookup.unreflect(method),
                            MethodType.methodType(Boolean.class, modelClass));

                    return (Function<Model, Boolean>) site.getTarget()
                            .invokeExact();
                })));
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
