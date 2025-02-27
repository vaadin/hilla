package com.vaadin.hilla.runtime.transfertypes;

import com.vaadin.hilla.transfertypes.annotations.FromModule;

@FromModule(
    module = "@vaadin/react-signals",
    namedSpecifiers = { "ListSignal" },
    defaultSpecifier = ""
)
public record ListSignal() {}
