/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.test.reactgrid;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.JavascriptExecutor;

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
        grid.scrollToRow(0);
        this.waitForLoading();
        grid.getRowCount();

        // The infinite data provider in auto grid will always add a 1 to
        // the total count until the last page is reached.
        // So we scroll down until we reach the last page and then check the
        // total count.
        grid.scrollToRow(3403034);
        this.waitForLoading();
        Assert.assertEquals(i, grid.getRowCount());
    }

    private void waitForLoading() {
        // Give some time for React to render changes and make sure Hilla
        // endpoints finish loading
        this.waitUntil((driver) -> {
            int loadingCount = ((Number) ((JavascriptExecutor) driver)
                    .executeAsyncScript(
                            "const resolve = arguments[arguments.length - 1];\n"
                                    + "globalThis.setTimeout(() => { resolve(globalThis.Vaadin.connectionState.loadingCount); }, 100)"))
                    .intValue();
            return loadingCount == 0;
        });
    }
}
