package dev.hilla.crud.filter;

/**
 * A filter for a given property that matches a string value using the given
 * matcher.
 */
public class PropertyStringFilter extends Filter {
    public enum Matcher {
        EQUALS, CONTAINS, LESS_THAN, GREATER_THAN;
    }

    private String propertyId;
    private String filterValue;
    private Matcher matcher;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher type) {
        this.matcher = type;
    }

    @Override
    public String toString() {
        return "PropertyStringFilter [propertyId=" + propertyId + ", matcher="
                + matcher + ", filterValue=" + filterValue + "]";
    }

}
