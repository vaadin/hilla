package com.vaadin.hilla.signals;

import com.fasterxml.jackson.databind.node.IntNode;
import com.vaadin.hilla.signals.core.SignalQueue;

public class NumberSignal extends SignalQueue<IntNode> {

    public NumberSignal(int defaultValue) {
        super(IntNode.valueOf(defaultValue));
    }

}
