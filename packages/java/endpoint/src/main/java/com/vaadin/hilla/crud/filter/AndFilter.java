/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.crud.filter;

import java.util.List;

/**
 * A filter that requires all children to pass.
 * <p>
 * Custom filter implementations need to handle this filter by running all child
 * filters and verifying that all of them pass.
 */
public class AndFilter extends Filter {

    private List<Filter> children;

    /**
     * Create an empty filter.
     */
    public AndFilter() {
        // Empty constructor is needed for serialization
    }

    /**
     * Create a filter with the given children.
     *
     * @param children
     *            the children of the filter
     */
    public AndFilter(Filter... children) {
        setChildren(List.of(children));
    }

    public List<Filter> getChildren() {
        return children;
    }

    public void setChildren(List<Filter> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "AndFilter [children=" + children + "]";
    }

}
