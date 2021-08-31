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

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

/**
 * A mapper between {@link Sort} and {@link SortDTO}.
 */
public class SortMapper implements Mapper<Sort, SortDTO> {

    @Override
    public Class<? extends Sort> getEndpointType() {
        return Sort.class;
    }

    @Override
    public Class<? extends SortDTO> getTransferType() {
        return SortDTO.class;
    }

    @Override
    public SortDTO toTransferType(Sort sort) {

        SortDTO sortDto = new SortDTO();
        List<OrderDTO> orders = new ArrayList<>();
        for (Order order : sort) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setProperty(order.getProperty());
            orderDTO.setDirection(order.getDirection());
            orderDTO.setIgnoreCase(order.isIgnoreCase());
            orderDTO.setNullHandling(order.getNullHandling());
            orders.add(orderDTO);
        }

        sortDto.setOrders(orders);
        return sortDto;
    }

    @Override
    public Sort toEndpointType(SortDTO sort) {
        if (sort == null) {
            return null;
        }
        List<Order> orders = new ArrayList<>();
        for (OrderDTO orderDto : sort.getOrders()) {
            Order order = new Order(orderDto.getDirection(),
                    orderDto.getProperty(), orderDto.getNullHandling());
            if (orderDto.isIgnoreCase()) {
                order = order.ignoreCase();
            }
            orders.add(order);
        }
        return Sort.by(orders);
    }

}
