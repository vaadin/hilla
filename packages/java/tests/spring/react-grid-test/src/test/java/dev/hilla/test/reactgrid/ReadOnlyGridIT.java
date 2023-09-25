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
        assertName(0, "Alice", "Johnson");
        assertName(8, "Ian", "Clark");
    }

    @Test
    public void sortingWorks() {
        assertName(0, "Alice", "Johnson");
        sortByColumn(0);
        assertName(0, "Abigail", "Carter");
        sortByColumn(0);
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
