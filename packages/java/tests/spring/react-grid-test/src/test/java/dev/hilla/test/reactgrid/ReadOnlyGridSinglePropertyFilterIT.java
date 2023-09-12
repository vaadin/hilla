package dev.hilla.test.reactgrid;

import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

public class ReadOnlyGridSinglePropertyFilterIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-single-property-filter";
    }

    @Test
    public void findsMatches() {
        setFilter("Ali");
        assertVisibleRows(1);
        assertName(0, "Alice", "Johnson");

        setFilter("");
        assertName(0, "Alice", "Johnson");
        assertVisibleRows(10);

        setFilter("xan");
        assertVisibleRows(2);
        assertName(0, "Xander", "Hill");
        assertName(1, "Xander", "Zane");

    }

    private void setFilter(String filter) {
        $(TextFieldElement.class).id("filter").setValue(filter);
    }

}
