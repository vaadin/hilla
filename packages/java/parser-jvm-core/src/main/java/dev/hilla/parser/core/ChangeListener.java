package dev.hilla.parser.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

class ChangeListener<T> {
    private final Supplier<T> current;
    private Runnable[] actions;
    private T former;

    public ChangeListener(Supplier<T> current) {
        this.current = current;
    }

    public void onChange(Runnable... actions) {
        this.actions = actions;
    }

    public void poll() {
        var current = this.current.get();

        if (!Objects.equals(current, former)) {
            former = current;
            Arrays.stream(actions).forEach(Runnable::run);
        }
    }
}
