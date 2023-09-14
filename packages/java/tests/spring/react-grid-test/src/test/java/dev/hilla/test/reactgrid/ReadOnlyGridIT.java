package dev.hilla.test.reactgrid;

import org.junit.Assert;
import org.junit.Test;

public class ReadOnlyGridIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid";
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

}
