package dev.hilla.parser.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

class ChangeListener<T> {
    private T former;
    private final Supplier<T> current;
    private Runnable[] actions;

    public ChangeListener(Supplier<T> current) {
        this.current = current;
    }

    public void poll() {
        var current = this.current.get();

        if (!Objects.equals(current, former)) {
            former = current;
            Arrays.stream(actions).forEach(Runnable::run);
        }
    }

    public void onChange(Runnable... actions) {
        this.actions = actions;
    }
}
