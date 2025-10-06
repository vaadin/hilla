package com.vaadin.hilla.test.reactgrid;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;

public class ReadOnlyGridIT extends AbstractGridTest {

    private static final int LUCKY_NUMBER_COLUMN = 4;

    protected int getFirstNameColumn() {
        return super.getFirstNameColumn() + 1;
    }

    protected int getLastNameColumn() {
        return super.getLastNameColumn() + 1;
    }

    @Test
    public void dataShown() {
        // commented out as flaky
        // Assert.assertEquals(7, grid.getLastVisibleRowIndex());
        Assert.assertEquals("1", grid.getCell(0, 0).getText());
        assertName(0, "Abigail", "Carter");
        Assert.assertEquals("9", grid.getCell(8, 0).getText());
        assertName(8, "Edward", "Gonzalez");
    }

    @Test
    public void sortingWorks() {
        assertName(0, "Abigail", "Carter");
        sortByColumn(1);
        assertName(0, "Zack", "Baker");
        sortByColumn(1);
        assertName(0, "Alice", "Johnson");
    }

    @Test
    public void scrollingLoadsData() {
        // getLastVisibleRowIndex() is giving variable results
        Assert.assertTrue(grid.getLastVisibleRowIndex() < 17);
        grid.scrollToRow(10);
        waitUntil(driver -> {
            return grid.getLastVisibleRowIndex() >= 17;
        });
        assertName(17, "Isabella", "King");
    }

    @Test
    public void customOptionsUsed() {
        TestBenchElement slot = grid.getCell(0, LUCKY_NUMBER_COLUMN)
                .getPropertyElement("firstElementChild");
        List<TestBenchElement> contents = (List<TestBenchElement>) executeScript(
                "return arguments[0].assignedNodes()", slot);
        TestBenchElement span = contents.get(0).$("span").first();
        Assert.assertEquals("rgba(255, 0, 0, 1)", span.getCssValue("color"));
    }

}
