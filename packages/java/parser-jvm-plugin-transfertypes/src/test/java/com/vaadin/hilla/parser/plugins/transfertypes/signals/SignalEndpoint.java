package com.vaadin.hilla.parser.plugins.transfertypes.signals;

import com.vaadin.hilla.signals.ListSignal;
import com.vaadin.hilla.signals.NumberSignal;
import com.vaadin.hilla.signals.Signal;
import com.vaadin.hilla.signals.ValueSignal;

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
