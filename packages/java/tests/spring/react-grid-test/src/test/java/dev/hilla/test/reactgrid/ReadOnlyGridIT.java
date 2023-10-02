package dev.hilla.test.reactgrid;

import org.junit.Assert;
import org.junit.Test;

public class ReadOnlyGridIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/readonly-grid";
    }

    @Test
    public void dataShown() {
        Assert.assertEquals(8, grid.getLastVisibleRowIndex());
        assertName(0, "Abigail", "Carter");
        assertName(8, "Edward", "Gonzalez");
    }

    @Test
    public void sortingWorks() {
        assertName(0, "Abigail", "Carter");
        sortByColumn(0);
        assertName(0, "Zack", "Baker");
        sortByColumn(0);
        assertName(0, "Alice", "Johnson");
    }

    @Test
    public void scrollingLoadsData() {
        Assert.assertEquals(8, grid.getLastVisibleRowIndex());
        grid.scrollToRow(10);
        waitUntil(driver -> {
            return grid.getLastVisibleRowIndex() == 18;
        });
        assertName(18, "Jack", "Lewis");
    }

}
