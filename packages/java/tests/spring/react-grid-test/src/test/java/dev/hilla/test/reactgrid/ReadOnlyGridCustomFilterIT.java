package dev.hilla.test.reactgrid;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;

public class ReadOnlyGridCustomFilterIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-custom-filter";
    }

    @Test
    public void findsMatches() {
        setFilterFullName("car");
        assertRowCount(2);
        assertName(0, "Abigail", "Carter");
        assertName(1, "Oscar", "Nelson");

        setFilterLastName("Nel");
        assertRowCount(1);
        assertName(0, "Oscar", "Nelson");

        setFilterFullName("");
        assertRowCount(2);
        assertName(0, "Leo", "Nelson");
        assertName(1, "Oscar", "Nelson");

        setFilterLastName("");
        assertRowCount(50);
    }

    private void setFilterFullName(String filter) {
        $(TextFieldElement.class).id("filter").setValue(filter);
    }

    private void setFilterLastName(String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, 1);
        TextFieldElement textFieldElement = cont.$(TextFieldElement.class)
                .first();
        textFieldElement.setValue(filter);
    }

}
