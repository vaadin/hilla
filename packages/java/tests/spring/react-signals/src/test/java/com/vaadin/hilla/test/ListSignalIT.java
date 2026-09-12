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
package com.vaadin.hilla.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.parallel.Browser;

/**
 * Tests that a list signal is kept in sync between two clients. The two clients
 * are two browser windows of the same driver, so the tests switch between them
 * by window handle.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ListSignalIT extends ChromeBrowserTest {

    private String firstWindow;
    private String secondWindow;

    @Override
    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();

        openListView();
        firstWindow = getDriver().getWindowHandle();
        // The list is shared by all tests, so it has to be emptied first
        clickButton("clearBtn");
        waitForItemCount(0);

        getDriver().switchTo().newWindow(WindowType.WINDOW);
        secondWindow = getDriver().getWindowHandle();
        openListView();
        waitForItemCount(0);

        switchToWindow(firstWindow);
    }

    @Test
    public void addedItem_isVisibleToTheOtherClient() {
        addItem("Buy milk");
        waitForItemCount(1);
        Assert.assertEquals(List.of("Buy milk"), getItemTexts());

        switchToWindow(secondWindow);
        waitForItemCount(1);
        Assert.assertEquals(List.of("Buy milk"), getItemTexts());
    }

    @Test
    public void addedItem_isVisibleToTheClientThatIsNotFocused() {
        switchToWindow(secondWindow);
        addItem("Write tests");
        waitForItemCount(1);

        switchToWindow(firstWindow);
        waitForItemCount(1);
        Assert.assertEquals(List.of("Write tests"), getItemTexts());
    }

    @Test
    public void toggledItem_isUpdatedForTheOtherClient() {
        addItem("Write tests");
        waitForItemCount(1);
        Assert.assertEquals(List.of("pending"), getItemStatuses());

        switchToWindow(secondWindow);
        waitForItemCount(1);
        Assert.assertEquals(List.of("pending"), getItemStatuses());

        // Toggling changes the value of the child signal of the entry, without
        // changing the structure of the list
        switchToWindow(firstWindow);
        clickFirst("toggleBtn");
        waitUntil(driver -> getItemStatuses().equals(List.of("done")));

        switchToWindow(secondWindow);
        waitUntil(driver -> getItemStatuses().equals(List.of("done")));
    }

    @Test
    public void removedItem_isRemovedForTheOtherClient() {
        addItem("Item A");
        waitForItemCount(1);
        addItem("Item B");
        waitForItemCount(2);

        switchToWindow(secondWindow);
        waitForItemCount(2);

        switchToWindow(firstWindow);
        clickFirst("removeBtn");
        waitForItemCount(1);
        Assert.assertEquals(List.of("Item B"), getItemTexts());

        switchToWindow(secondWindow);
        waitForItemCount(1);
        Assert.assertEquals(List.of("Item B"), getItemTexts());
    }

    @Test
    public void clearedList_isClearedForTheOtherClient() {
        addItem("Item 1");
        waitForItemCount(1);
        addItem("Item 2");
        waitForItemCount(2);

        switchToWindow(secondWindow);
        waitForItemCount(2);
        clickButton("clearBtn");
        waitForItemCount(0);

        switchToWindow(firstWindow);
        waitForItemCount(0);
    }

    private void openListView() {
        getDriver().get(getRootURL() + "/SharedListSignal");
        waitForElementPresent(By.id("itemCount"));
        waitUntil(driver -> !$("h3").id("itemCount").getText().isEmpty());
    }

    private void switchToWindow(String handle) {
        getDriver().switchTo().window(handle);
    }

    private void addItem(String text) {
        var input = getDriver().findElement(By.id("newItemInput"));
        input.clear();
        input.sendKeys(text);
        clickButton("addItemBtn");
    }

    private void waitForItemCount(int count) {
        waitUntil(driver -> $("h3").id("itemCount").getText()
                .equals("Count: " + count));
    }

    private List<String> getItemTexts() {
        return getTexts("[data-testid='item'] span:first-child");
    }

    private List<String> getItemStatuses() {
        return getTexts("[data-testid='item'] span:nth-child(2)");
    }

    private List<String> getTexts(String selector) {
        return getDriver().findElements(By.cssSelector(selector)).stream()
                .map(WebElement::getText).toList();
    }

    private void clickFirst(String testId) {
        getDriver()
                .findElement(By.cssSelector("[data-testid='" + testId + "']"))
                .click();
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }
}
