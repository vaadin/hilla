package com.example.application;

import org.junit.Assert;
import org.junit.Test;

public class ReadOnlyGridIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/grid";
    }

    @Test
    public void dataShown() {
        Assert.assertEquals(8, grid.getLastVisibleRowIndex());
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
        Assert.assertEquals(8, grid.getLastVisibleRowIndex());
        grid.scrollToRow(10);
        waitUntil(driver -> {
            return grid.getLastVisibleRowIndex() == 18;
        });
        assertName(18, "Samuel", "Turner");
    }

}
