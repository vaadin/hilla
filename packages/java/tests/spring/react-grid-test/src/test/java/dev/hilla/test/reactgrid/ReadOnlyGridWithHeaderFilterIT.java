package dev.hilla.test.reactgrid;

import dev.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Test;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;

public class ReadOnlyGridWithHeaderFilterIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-with-headerfilters";
    }

    @Test
    public void stringMatchesContains() {
        setHeaderFilter(2, "al");
        assertRowCount(1);
        assertName(0, "Alice", "Johnson");

        // Test that we are and:ing
        setHeaderFilter(3, "j");
        assertRowCount(1);
        assertName(0, "Alice", "Johnson");

        setHeaderFilter(2, "");
        assertRowCount(2);
        assertName(0, "Alice", "Johnson");
        assertName(1, "Henry", "Jackson");
    }

    @Test
    public void numberFilterWorks() {
        setHeaderFilter(0, null, "40"); // Default is greater_than
        assertRowCount(10);
        assertName(0, "Oliver", "Quinn");

        setHeaderFilter(0, Matcher.LESS_THAN, null);
        assertRowCount(39);
        assertName(0, "Alice", "Johnson");

        setHeaderFilter(0, Matcher.EQUALS, "30");
        assertRowCount(1);
        assertName(0, "Dylan", "Fisher");

    }

    private void setHeaderFilter(int columnIndex, String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, columnIndex);
        TextFieldElement filterField = cont.$(TextFieldElement.class).first();
        filterField.setValue(filter);
    }

    private void setHeaderFilter(int columnIndex, Matcher matcher,
            String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, columnIndex);
        SelectElement filterSelect = cont.$(SelectElement.class).first();
        if (matcher == Matcher.GREATER_THAN) {
            filterSelect.setProperty("value", "GREATER_THAN");
        } else if (matcher == Matcher.LESS_THAN) {
            filterSelect.setProperty("value", "LESS_THAN");
        } else if (matcher == Matcher.EQUALS) {
            filterSelect.setProperty("value", "EQUALS");
        }
        TextFieldElement filterField = cont.$(TextFieldElement.class).first();
        if (filter != null) {
            filterField.setValue(filter);
        }
    }

}
