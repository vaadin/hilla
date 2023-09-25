package dev.hilla.test.reactgrid;

import org.junit.Assert;
import org.junit.Before;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public abstract class AbstractGridTest extends ChromeBrowserTest {

    protected GridElement grid;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
        grid = $(GridElement.class).first();
        waitUntil(driver -> {
            Object prop = grid.getProperty("_lastVisibleIndex");
            return prop != null;
        });
    }

    protected void assertName(int row, String firstName, String lastName) {
        assertFirstName(row, firstName);
        assertLastName(row, lastName);
    }

    protected void assertFirstName(int row, String firstName) {
        Assert.assertEquals(firstName, grid.getCell(row, 2).getText());
    }

    protected void assertLastName(int row, String lastName) {
        Assert.assertEquals(lastName, grid.getCell(row, 3).getText());
    }

    protected void sortByColumn(int i) {
        grid.getHeaderCell(i).$("vaadin-grid-sorter").first().click();
    }

    protected void assertRowCount(int i) {
        grid.scrollToRow(3403034);
        waitUntil(driver -> {
            return grid.getRowCount() == i;
        });

    }

}
