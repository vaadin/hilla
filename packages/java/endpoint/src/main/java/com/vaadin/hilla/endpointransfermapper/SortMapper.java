/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.endpointransfermapper;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.hilla.endpointransfermapper.EndpointTransferMapper.Mapper;
import com.vaadin.hilla.mappedtypes.Order;
import com.vaadin.hilla.mappedtypes.Sort;

/**
 * A mapper between {@link org.springframework.data.domain.Sort} and
 * {@link Sort}.
 */
public class SortMapper
        implements Mapper<org.springframework.data.domain.Sort, Sort> {

    @Override
    public Class<? extends org.springframework.data.domain.Sort> getEndpointType() {
        return org.springframework.data.domain.Sort.class;
    }

    @Override
    public Class<? extends Sort> getTransferType() {
        return Sort.class;
    }

    @Override
    public Sort toTransferType(org.springframework.data.domain.Sort sort) {

        Sort transferSort = new Sort();
        List<Order> transferOrders = new ArrayList<>();
        for (org.springframework.data.domain.Sort.Order order : sort) {
            Order transferOrder = new Order();
            transferOrder.setProperty(order.getProperty());
            transferOrder.setDirection(order.getDirection());
            transferOrder.setIgnoreCase(order.isIgnoreCase());
            transferOrder.setNullHandling(order.getNullHandling());
            transferOrders.add(transferOrder);
        }

        transferSort.setOrders(transferOrders);
        return transferSort;
    }

    @Override
    public org.springframework.data.domain.Sort toEndpointType(
            Sort transferSort) {
        if (transferSort == null) {
            return null;
        }
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        for (Order transferOrder : transferSort.getOrders()) {
            org.springframework.data.domain.Sort.Order order = new org.springframework.data.domain.Sort.Order(
                    transferOrder.getDirection(), transferOrder.getProperty(),
                    transferOrder.getNullHandling());
            if (transferOrder.isIgnoreCase()) {
                order = order.ignoreCase();
            }
            orders.add(order);
        }
        return org.springframework.data.domain.Sort.by(orders);
    }

}
