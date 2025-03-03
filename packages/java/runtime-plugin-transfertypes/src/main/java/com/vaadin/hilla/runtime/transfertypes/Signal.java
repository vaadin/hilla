package com.vaadin.hilla.runtime.transfertypes;

import com.vaadin.hilla.transfertypes.annotations.FromModule;

@FromModule(
    module = "@vaadin/hilla-react-signals",
    namedSpecifier = "Signal"
)
public record Signal() {}
