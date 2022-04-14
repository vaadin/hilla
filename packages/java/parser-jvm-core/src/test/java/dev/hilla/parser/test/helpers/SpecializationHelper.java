package dev.hilla.parser.test.helpers;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import dev.hilla.parser.models.SpecializedModel;

public class SpecializationHelper {
    private final Map<String, Supplier<Boolean>> specializations = new HashMap<>();

    public SpecializationHelper(SpecializedModel model) {
        try {
            var lookup = MethodHandles.lookup();

            for (var method : SpecializedModel.class.getDeclaredMethods()) {
                var returnType = MethodType.methodType(Object.class);

                var site = LambdaMetafactory.metafactory(lookup, "get",
                        MethodType.methodType(Supplier.class,
                                SpecializedModel.class),
                        returnType, lookup.unreflect(method), returnType);

                var supplier = (Supplier<Boolean>) site.getTarget()
                        .invoke(model);

                specializations.put(method.getName(), supplier);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void apply(BiConsumer<String, Supplier<Boolean>> checker) {
        specializations.forEach(checker);
    }
}
