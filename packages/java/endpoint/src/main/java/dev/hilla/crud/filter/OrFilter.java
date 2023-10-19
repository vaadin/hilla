package dev.hilla.crud.filter;

import java.util.List;

/**
 * A filter that requires at least one of its children to pass.
 */
public class OrFilter extends Filter {

    private List<Filter> children;

    public List<Filter> getChildren() {
        return children;
    }

    public void setChildren(List<Filter> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "OrFilter [children=" + children + "]";
    }

}
