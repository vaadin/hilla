package com.vaadin.hilla.signals.operation;

public interface ValueOperation<T> extends SignalOperation<T> {
    T value();
}
