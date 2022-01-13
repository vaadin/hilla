package com.vaadin.fusion.parser.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

final class Pool {
    private static final Map<Object, Relative<?>> pool = new HashMap<>();

    private Pool() {
    }

    public static <Member, RelativeMember extends Relative<?>, Parent extends Relative<?>> RelativeMember createInstance(
            @Nonnull Member member, Parent parent,
            BiFunction<Member, Parent, RelativeMember> initializer) {
        var result = pool.get(Objects.requireNonNull(member));

        if (result != null) {
            return (RelativeMember) result;
        }

        var wrapped = initializer.apply(member, parent);
        pool.put(member, wrapped);

        return wrapped;
    }

    static void clear() {
        pool.clear();
    }
}
