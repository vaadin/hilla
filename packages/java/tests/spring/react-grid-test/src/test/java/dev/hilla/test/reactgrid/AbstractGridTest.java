package dev.hilla.test.reactgrid;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
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
        grid.scrollToRow(3403034);
        waitUntil(driver -> {
            return grid.getRowCount() == i;
        });

    }

}
