package com.example.application;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Before;

public abstract class AbstractGridTest extends ChromeBrowserTest {

    protected GridElement grid;

    @Override
    public int getDeploymentPort() {
        return 8080;
    }

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
        grid = $(GridElement.class).first();
        // reset default sort order
        sortByColumn(0);
        sortByColumn(0);
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
        Assert.assertEquals(firstName, grid.getCell(row, 0).getText());
    }

    protected void assertLastName(int row, String lastName) {
        Assert.assertEquals(lastName, grid.getCell(row, 1).getText());
    }

    protected void sortByColumn(int i) {
        grid.getHeaderCellContent(0, i).$("vaadin-grid-sorter").first().click();
    }

    protected void assertRowCount(int i) {
        grid.scrollToRow(3403034);
        waitUntil(driver -> {
            return grid.getRowCount() == i;
        });

    }

}
