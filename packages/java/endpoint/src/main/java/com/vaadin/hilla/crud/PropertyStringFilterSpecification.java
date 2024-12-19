package com.vaadin.hilla.crud;

import com.vaadin.flow.spring.data.filter.PropertyStringFilter;

public class PropertyStringFilterSpecification<T> extends
        com.vaadin.flow.spring.data.jpa.PropertyStringFilterSpecification<T> {

    public PropertyStringFilterSpecification(PropertyStringFilter filter) {
        super(filter);
    }

}
