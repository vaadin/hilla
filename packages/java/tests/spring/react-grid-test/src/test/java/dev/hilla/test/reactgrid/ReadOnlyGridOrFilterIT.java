package dev.hilla.test.reactgrid;

import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

public class ReadOnlyGridOrFilterIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-or-filter";
    }

    @Test
    public void findsMatches() {
        setFilter("car");
        assertVisibleRows(2);
        assertName(0, "Oscar", "Nelson");
        assertName(1, "Abigail", "Carter");

        setFilter("");
        assertVisibleRows(10);

        setFilter("zan");
        assertVisibleRows(1);
        assertName(0, "Xander", "Zane");

    }

    private void setFilter(String filter) {
        $(TextFieldElement.class).id("filter").setValue(filter);
    }

}
