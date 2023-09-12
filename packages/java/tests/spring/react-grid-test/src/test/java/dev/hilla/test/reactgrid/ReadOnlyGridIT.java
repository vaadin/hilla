package dev.hilla.test.reactgrid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public class ReadOnlyGridIT extends ChromeBrowserTest {

    private GridElement grid;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/readonly-grid");
        grid = $(GridElement.class).first();
        waitUntil(driver -> {
            Object prop = grid.getProperty("_lastVisibleIndex");
            return prop != null;
        });
    }

    @Test
    public void dataShown() {
        Assert.assertEquals(9, grid.getLastVisibleRowIndex());
        Assert.assertEquals("1", grid.getCell(0, 0).getText());
        assertName(0, "Alice", "Johnson");

        Assert.assertEquals("9", grid.getCell(8, 0).getText());
        assertName(8, "Ian", "Clark");
    }

    @Test
    public void sortingWorks() {
        assertName(0, "Alice", "Johnson");
        sortByColumn(2);
        assertName(0, "Abigail", "Carter");
        sortByColumn(2);
        assertName(0, "Zack", "Baker");
    }

    @Test
    public void scrollingLoadsData() {
        Assert.assertEquals(9, grid.getLastVisibleRowIndex());
        grid.scrollToRow(10);
        waitUntil(driver -> {
            return grid.getLastVisibleRowIndex() == 19;
        });
        assertName(19, "Tina", "Phillips");
    }

    private void assertName(int row, String firstName, String lastName) {
        assertFirstName(row, firstName);
        assertLastName(row, lastName);
    }

    private void assertFirstName(int row, String firstName) {
        Assert.assertEquals(firstName, grid.getCell(row, 2).getText());
    }

    private void assertLastName(int row, String lastName) {
        Assert.assertEquals(lastName, grid.getCell(row, 3).getText());
    }

    private void sortByColumn(int i) {
        grid.getHeaderCell(i).$("vaadin-grid-sorter").first().click();
    }
}
