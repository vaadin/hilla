/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.parser.plugins.nonnull.superclassmethods;

import java.util.Optional;

@EndpointExposed
public class ReadOnlyEndpoint<T, ID> extends NonEndpointImpl
        implements NonEndpoint {
    public Optional<T> get(ID id) {
        return Optional.ofNullable(null);
    }
}
