/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.mappedtypes;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;

/**
 * A DTO for {@code org.springframework.data.domain.Sort}.
 */
public class Sort {
    @Nonnull
    private List<Order> orders = new ArrayList<>();

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}
