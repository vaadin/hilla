package com.vaadin.hilla.test.reactgrid;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public abstract class AbstractGridTest extends ChromeBrowserTest {

    protected GridElement grid;

    @Override
    protected String getTestPath() {
        return getRootURL() + "/"
                + getClass().getSimpleName().replace("IT", "");

    }

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
        grid = $(GridElement.class).waitForFirst();
        waitUntil(driver -> {
            Object prop = grid.getProperty("_lastVisibleIndex");
            return prop != null;
        });
    }

    @After
    public void checkBrowserLogs() {
        checkLogsForErrors();
    }

    protected void assertColumns(String... expected) {
        List<String> actual = grid.getVisibleColumns().stream()
                .map(column -> column.getHeaderCell().getText()).toList();
        Assert.assertEquals(List.of(expected), actual);

    }

    protected void assertRow(int row, String... expected) {
        waitUntil(_driver -> grid.getRowCount() > 0);
        GridTRElement tr = grid.getRow(row);
        List<String> actual = grid.getVisibleColumns().stream()
                .map(col -> tr.getCell(col).getText()).toList();

        Assert.assertEquals(List.of(expected), actual);
    }

    protected void assertName(int row, String firstName, String lastName) {
        assertFirstName(row, firstName);
        assertLastName(row, lastName);
    }

    protected int getFirstNameColumn() {
        return 0;
    }

    protected int getLastNameColumn() {
        return 1;
    }

    protected void assertFirstName(int row, String firstName) {
        Assert.assertEquals(firstName,
                grid.getCell(row, getFirstNameColumn()).getText());
    }

    protected void assertLastName(int row, String lastName) {
        Assert.assertEquals(lastName,
                grid.getCell(row, getLastNameColumn()).getText());
    }

    protected void sortByColumn(int i) {
        grid.getHeaderCellContent(0, i).$("vaadin-grid-sorter").first().click();
    }

    protected void assertRowCount(int i) {
        // Workaround to prevent scrollToRow from stalling, likely because
        // this waits until the grid has finished loading
        grid.getRowCount();
        // The infinite data provider in auto grid will always add a 1 to
        // the total count until the last page is reached.
        // So we scroll down until we reach the last page and then check the
        // total count.
        grid.scrollToRow(3403034);
        Assert.assertEquals(i, grid.getRowCount());
    }

}
