package dev.hilla.test.reactgrid;

import java.util.Locale;
import java.util.function.Consumer;

import dev.hilla.crud.filter.PropertyStringFilter.Matcher;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

public class ReadOnlyGridWithHeaderFilterIT extends AbstractGridTest {

    private static final int FIRST_NAME_COLUMN = 0;
    private static final int LAST_NAME_COLUMN = 1;
    private static final int LUCKY_NUMBER_COLUMN = 2;
    private static final int EMAIL_VERIFIED_COLUMN = 3;

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid-with-headerfilters";
    }

    @Test
    public void stringMatchesContains() {
        setHeaderFilter(FIRST_NAME_COLUMN, "al");
        assertRowCount(1);
        assertName(0, "Alice", "Johnson");

        // Test that we are and:ing
        setHeaderFilter(LAST_NAME_COLUMN, "j");
        assertRowCount(1);
        assertName(0, "Alice", "Johnson");

        setHeaderFilter(FIRST_NAME_COLUMN, "");
        assertRowCount(2);
        assertName(0, "Alice", "Johnson");
        assertName(1, "Henry", "Jackson");
    }

    @Test
    public void numberFilterWorks() {
        // Default is greater_than
        assertRowCount(50);
        sortByColumn(LUCKY_NUMBER_COLUMN);
        setHeaderFilter(LUCKY_NUMBER_COLUMN, null, "150");
        assertName(0, "George", "Lee");
        assertRowCount(4);

        setHeaderFilter(LUCKY_NUMBER_COLUMN, Matcher.LESS_THAN, (String) null);
        assertRowCount(50 - 4);
        assertName(0, "Dylan", "Fisher");

        setHeaderFilter(LUCKY_NUMBER_COLUMN, Matcher.EQUALS, "25");
        assertRowCount(1);
        assertName(0, "Thomas", "Vance");

    }

    @Test
    public void booleanFilterWorks() {
        // Default is greater_than
        assertRowCount(50);
        setHeaderFilter(EMAIL_VERIFIED_COLUMN, null, true);
        assertName(0, "Abigail", "Carter");
        assertRowCount(42);

        setHeaderFilter(EMAIL_VERIFIED_COLUMN, null, false);
        assertRowCount(50 - 42);
        assertName(0, "Catherine", "Evans");

        setHeaderFilter(EMAIL_VERIFIED_COLUMN, null, (Boolean) null);
        assertRowCount(50);
        assertName(0, "Abigail", "Carter");

    }

    @Test
    public void numberFilterWithInvalidInputIgnored() {
        setHeaderFilter(LUCKY_NUMBER_COLUMN, null, "a");
        assertRowCount(50);
        setHeaderFilter(LUCKY_NUMBER_COLUMN, null, "10000");
        assertRowCount(1);
    }

    private void setHeaderFilter(int columnIndex, String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, columnIndex);
        TextFieldElement filterField = cont.$(TextFieldElement.class).first();
        filterField.setValue(filter);
    }

    private void setHeaderFilter(int columnIndex, Matcher matcher,
            Boolean filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, columnIndex);
        SelectElement filterSelect = cont.$(SelectElement.class).first();
        String text = filter == null ? "" : filter ? "Yes" : "No";
        filterSelect.openPopup();

        filterSelect.getPropertyElement("_overlayElement").$("vaadin-item")
                .attribute("label", text).first().click();

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
        TestBenchElement filterField = filterSelect
                .getPropertyElement("nextElementSibling");
        if (filter != null) {
            ifType(filterField, TextFieldElement.class,
                    e -> e.setValue(filter));
            ifType(filterField, NumberFieldElement.class,
                    e -> e.setValue(filter));
        }
    }

    private <T extends TestBenchElement> void ifType(TestBenchElement element,
            Class<T> type, Consumer<T> cmd) {
        Element annotation = type.getAnnotation(Element.class);
        if (element.getTagName().toLowerCase(Locale.ENGLISH)
                .equals(annotation.value().toLowerCase(Locale.ENGLISH))) {
            cmd.accept(TestBench.wrap(element, type));
        }
    }

}
