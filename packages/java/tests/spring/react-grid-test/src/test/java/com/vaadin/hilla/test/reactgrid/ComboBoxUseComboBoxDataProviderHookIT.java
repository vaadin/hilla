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
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class ComboBoxUseComboBoxDataProviderHookIT extends ChromeBrowserTest {

    @Override
    protected String getTestPath() {
        return getRootURL() + "/"
                + getClass().getSimpleName().replace("IT", "");

    }

    @Override
    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
    }

    @After
    public void checkBrowserLogs() {
        checkLogsForErrors();
    }

    @Test
    public void defaultSort_dataShown() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .withId("defaultSort").waitForFirst();
        List<String> options = getOptions(comboBox);
        Assert.assertEquals("Johnson", options.get(0));
        Assert.assertEquals("Lewis", options.get(9));

    }

    @Test
    public void sortUsingLastname_dataShown() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .withId("sortLastName").waitForFirst();
        List<String> options = getOptions(comboBox);
        Assert.assertEquals("Adams", options.get(0));
        Assert.assertEquals("Evans", options.get(9));
    }

    @Test
    public void filteringUsingSignalWorks() {
        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .withId("prependFilter").waitForFirst();
        List<String> options = getOptions(comboBox);
        int size = options.size();
        Assert.assertEquals("Adams", options.get(0));
        Assert.assertEquals("Evans", options.get(9));
        comboBox.closePopup();

        TestBenchElement filterInput = $("input").withId("filter")
                .waitForFirst();
        filterInput.sendKeys("c");
        waitUntil(driver -> getOptions(comboBox).size() < size);
        options = getOptions(comboBox);
        Assert.assertEquals("Baker", options.get(0)); // Zack
        Assert.assertEquals("Johnson", options.get(9)); // Alice
    }

    private List<String> getOptions(ComboBoxElement comboBox) {
        comboBox.openPopup();
        return waitUntil(driver -> {
            List<String> opt = comboBox.getOptions();
            if (opt.isEmpty()) {
                return null;
            }
            return opt;
        });
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
