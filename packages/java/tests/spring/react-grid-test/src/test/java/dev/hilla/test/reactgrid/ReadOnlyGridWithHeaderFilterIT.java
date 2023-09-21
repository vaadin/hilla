package dev.hilla.test.reactgrid;

import org.junit.Test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;

public class ReadOnlyGridWithHeaderFilterIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-with-headerfilters";
    }

    @Test
    public void stringMatchesContains() {
        setHeaderFilter(2, "al");
        assertVisibleRows(1);
        assertName(0, "Alice", "Johnson");

        // Test that we are and:ing
        setHeaderFilter(3, "j");
        assertVisibleRows(1);
        assertName(0, "Alice", "Johnson");

        setHeaderFilter(2, "");
        assertVisibleRows(2);
        assertName(0, "Alice", "Johnson");
        assertName(1, "Henry", "Jackson");
    }

    private void setHeaderFilter(int columnIndex, String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, columnIndex);
        TextFieldElement filterField = cont.$(TextFieldElement.class).first();
        filterField.setValue(filter);
    }

}
