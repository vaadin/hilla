/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.fusion.endpointransfermapper;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.fusion.endpointransfermapper.EndpointTransferMapper.Mapper;
import com.vaadin.fusion.mappedtypes.Order;
import com.vaadin.fusion.mappedtypes.Sort;

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
