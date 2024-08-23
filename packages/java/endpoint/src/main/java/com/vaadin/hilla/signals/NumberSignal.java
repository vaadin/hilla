package com.vaadin.hilla.signals;

/**
 * A signal that holds a number value.
 */
public class NumberSignal extends ValueSignal<Double> {

    /**
     * Creates a new NumberSignal with the provided default value.
     *
     * @param defaultValue
     *            the default value
     *
     * @throws NullPointerException
     *             if the default value is null
     */
    public NumberSignal(Double defaultValue) {
        super(defaultValue);
    }

    /**
     * Creates a new NumberSignal with the default value of 0.
     */
    public NumberSignal() {
        this(0.0);
    }
}
