package com.vaadin.hilla.parser.plugins.transfertypes.signals;

import com.vaadin.signals.ListSignal;
import com.vaadin.signals.NumberSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;

@Endpoint
public class SignalEndpoint {
    public Signal<String> getStringSignal() {
        return null;
    }

    public ValueSignal<String> getStringValueSignal() {
        return null;
    }

    public NumberSignal getNumberSignal() {
        return null;
    }

    public ListSignal<String> getStringListSignal() {
        return null;
    }
}
