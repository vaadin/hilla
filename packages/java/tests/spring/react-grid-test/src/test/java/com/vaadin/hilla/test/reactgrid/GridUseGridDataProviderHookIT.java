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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.testbench.TestBenchElement;

public class GridUseGridDataProviderHookIT extends AbstractGridTest {

    @Test
    public void sortingWorks_and_refreshingDataProvider_keepsTheAppliedSort() {
        assertFirstName(0, "Alice");
        assertFirstName(1, "Bob");
        assertFirstName(2, "Charlie");
        assertFirstName(3, "David");

        sortByColumn(0); // sort by firstName ascending
        assertFirstName(0, "Abigail");
        assertFirstName(1, "Alice");
        assertFirstName(2, "Benjamin");
        assertFirstName(3, "Bob");

        sortByColumn(0); // sort by firstName descending
        assertFirstName(0, "Zack");
        assertFirstName(1, "Yasmine");
        assertFirstName(2, "Xander");
        assertFirstName(3, "Xander");

        refresh();

        assertFirstName(0, "Zack");
        assertFirstName(1, "Yasmine");
        assertFirstName(2, "Xander");
        assertFirstName(3, "Xander");
    }

    @Test
    public void filteringUsingSignalWorks() {
        assertFirstName(0, "Alice");
        assertFirstName(1, "Bob");
        assertFirstName(2, "Charlie");
        assertFirstName(3, "David");

        setFilter("al");
        assertFirstName(0, "Alice");
        assertFirstName(1, "Edward"); // Gonazlez matches
        assertFirstName(2, "Kathy"); // Walker matches
        assertFirstName(3, "Laura"); // Hall matches
        setFilter("");
        assertFirstName(0, "Alice");
        assertFirstName(1, "Bob");
        assertFirstName(2, "Charlie");
        assertFirstName(3, "David");

    }

    private void setFilter(String string) {
        TestBenchElement filterInput = $("input").first();
        filterInput.clear();
        filterInput.sendKeys(string);
        filterInput.dispatchEvent("input", Map.of("bubbles", true));
    }

    private void refresh() {
        var refreshButton = $(ButtonElement.class).all().stream()
                .filter(button -> button.getText().equals("Refresh"))
                .findFirst();
        if (refreshButton.isPresent()) {
            refreshButton.get().click();
        } else {
            Assert.fail("Refresh button not found");
        }
    }
}
