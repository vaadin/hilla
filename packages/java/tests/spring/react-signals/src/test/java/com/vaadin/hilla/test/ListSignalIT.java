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

@RunWith(BlockJUnit4ClassRunner.class)
public class ListSignalIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();
        getDriver().get(getRootURL() + "/SharedListSignal");
        waitForPageToLoad();
        // Start with a clean list
        clickButton("clearBtn");
        waitForMillis(500);
    }

    private void waitForPageToLoad() {
        waitForElementPresent(By.id("itemCount"));
        waitUntil(driver -> $("h3").id("itemCount").getText() != null);
        waitForMillis(1000);
    }

    @Test
    public void shouldAddItem_andSyncToOtherClient() {
        var firstWindow = getDriver().getWindowHandle();

        // Open second window
        var secondWindow = getDriver().switchTo()
                .newWindow(WindowType.WINDOW);
        try {
            secondWindow.get(getRootURL() + "/SharedListSignal");
            waitForPageToLoad();

            // Add item from second window
            secondWindow.findElement(By.id("newItemInput")).sendKeys("Buy milk");
            secondWindow.findElement(By.id("addItemBtn")).click();
            waitForMillis(500);

            // Verify item appears in second window
            Assert.assertEquals("Count: 1",
                    secondWindow.findElement(By.id("itemCount")).getText());

            // Verify item synced to first window
            getDriver().switchTo().window(firstWindow);
            waitForMillis(500);
            Assert.assertEquals("Count: 1",
                    $("h3").id("itemCount").getText());

            // Verify item text
            var items = getItemTexts();
            Assert.assertEquals(1, items.size());
            Assert.assertEquals("Buy milk", items.get(0));
        } finally {
            secondWindow.close();
            getDriver().switchTo().window(firstWindow);
        }
    }

    @Test
    public void shouldToggleChildValue_andSyncToOtherClient() {
        // Add an item
        setInputValue("newItemInput", "Write tests");
        clickButton("addItemBtn");
        waitForMillis(500);

        Assert.assertEquals("Count: 1", $("h3").id("itemCount").getText());

        // Verify initial status is pending
        var statuses = getItemStatuses();
        Assert.assertEquals(1, statuses.size());
        Assert.assertEquals("pending", statuses.get(0));

        var firstWindow = getDriver().getWindowHandle();

        // Open second window
        var secondWindow = getDriver().switchTo()
                .newWindow(WindowType.WINDOW);
        try {
            secondWindow.get(getRootURL() + "/SharedListSignal");
            waitForPageToLoad();

            // Verify item exists in second window
            Assert.assertEquals("Count: 1",
                    secondWindow.findElement(By.id("itemCount")).getText());

            // Toggle item completion from first window
            getDriver().switchTo().window(firstWindow);
            clickFirstToggleButton();
            waitForMillis(500);

            // Verify status changed locally
            statuses = getItemStatuses();
            Assert.assertEquals("done", statuses.get(0));

            // Verify status synced to second window
            getDriver().switchTo().window(secondWindow.getWindowHandle());
            waitForMillis(500);

            var secondWindowStatuses = getDriver()
                    .findElements(By.cssSelector("[data-testid='item'] span:nth-child(2)"))
                    .stream()
                    .map(WebElement::getText)
                    .toList();
            Assert.assertEquals(1, secondWindowStatuses.size());
            Assert.assertEquals("done", secondWindowStatuses.get(0));
        } finally {
            secondWindow.close();
            getDriver().switchTo().window(firstWindow);
        }
    }

    @Test
    public void shouldRemoveItem_andSyncToOtherClient() {
        // Add two items
        setInputValue("newItemInput", "Item A");
        clickButton("addItemBtn");
        waitForMillis(300);
        setInputValue("newItemInput", "Item B");
        clickButton("addItemBtn");
        waitForMillis(500);

        Assert.assertEquals("Count: 2", $("h3").id("itemCount").getText());

        var firstWindow = getDriver().getWindowHandle();

        // Open second window
        var secondWindow = getDriver().switchTo()
                .newWindow(WindowType.WINDOW);
        try {
            secondWindow.get(getRootURL() + "/SharedListSignal");
            waitForPageToLoad();

            // Verify both items in second window
            Assert.assertEquals("Count: 2",
                    secondWindow.findElement(By.id("itemCount")).getText());

            // Remove first item from first window
            getDriver().switchTo().window(firstWindow);
            clickFirstRemoveButton();
            waitForMillis(500);

            Assert.assertEquals("Count: 1",
                    $("h3").id("itemCount").getText());

            // Verify removal synced to second window
            getDriver().switchTo().window(secondWindow.getWindowHandle());
            waitForMillis(500);
            Assert.assertEquals("Count: 1",
                    secondWindow.findElement(By.id("itemCount")).getText());
        } finally {
            secondWindow.close();
            getDriver().switchTo().window(firstWindow);
        }
    }

    @Test
    public void shouldClearAll_andSyncToOtherClient() {
        // Add items
        setInputValue("newItemInput", "Item 1");
        clickButton("addItemBtn");
        waitForMillis(300);
        setInputValue("newItemInput", "Item 2");
        clickButton("addItemBtn");
        waitForMillis(500);

        var firstWindow = getDriver().getWindowHandle();

        var secondWindow = getDriver().switchTo()
                .newWindow(WindowType.WINDOW);
        try {
            secondWindow.get(getRootURL() + "/SharedListSignal");
            waitForPageToLoad();

            // Clear from second window
            secondWindow.findElement(By.id("clearBtn")).click();
            waitForMillis(500);

            Assert.assertEquals("Count: 0",
                    secondWindow.findElement(By.id("itemCount")).getText());

            // Verify first window is cleared
            getDriver().switchTo().window(firstWindow);
            waitForMillis(500);
            Assert.assertEquals("Count: 0",
                    $("h3").id("itemCount").getText());
        } finally {
            secondWindow.close();
            getDriver().switchTo().window(firstWindow);
        }
    }

    private List<String> getItemTexts() {
        return getDriver()
                .findElements(By.cssSelector(
                        "[data-testid='item'] span:first-child"))
                .stream().map(WebElement::getText).toList();
    }

    private List<String> getItemStatuses() {
        return getDriver()
                .findElements(By.cssSelector(
                        "[data-testid='item'] span:nth-child(2)"))
                .stream().map(WebElement::getText).toList();
    }

    private void clickFirstToggleButton() {
        getDriver()
                .findElement(By.cssSelector("[data-testid='toggleBtn']"))
                .click();
    }

    private void clickFirstRemoveButton() {
        getDriver()
                .findElement(By.cssSelector("[data-testid='removeBtn']"))
                .click();
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }

    private void setInputValue(String id, String value) {
        var input = getDriver().findElement(By.id(id));
        input.clear();
        input.sendKeys(value);
    }

    private void waitForMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
