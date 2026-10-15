/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */

package com.vaadin.hilla.parser.plugins.nonnull.superclassmethods;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@EndpointExposed
public interface PagedData<T> {
    default List<@Nonnull T> getNonNullablePage(int pageSize, int pageNumber,
            Map<String, @Nonnull T> parameters) {
        return Collections.emptyList();
    }

    default List<T> getPage(int pageSize, int pageNumber) {
        return Collections.emptyList();
    }

    default int size() {
        return 0;
    }
}
