/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;

/**
 * The auto grid reads the data through the generated client of a
 * <code>CrudRepositoryService</code>, so this covers an endpoint call, the JPA
 * entity behind it and the sorting and paging the grid asks for.
 */
public class ReadOnlyGridIT extends AbstractNativeIT {

    private GridElement grid;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open("/grid");
        grid = $(GridElement.class).waitForFirst();
        // reset default sort order
        sortByColumn(0);
        sortByColumn(0);
        waitUntil(driver -> grid.getProperty("_lastVisibleIndex") != null);
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
        waitUntil(driver -> grid.getLastVisibleRowIndex() == 18);
        assertName(18, "Samuel", "Turner");
    }

    private void assertName(int row, String firstName, String lastName) {
        Assert.assertEquals(firstName, grid.getCell(row, 0).getText());
        Assert.assertEquals(lastName, grid.getCell(row, 1).getText());
    }

    private void sortByColumn(int column) {
        grid.getHeaderCellContent(0, column).$("vaadin-grid-sorter").first()
                .click();
    }

}
