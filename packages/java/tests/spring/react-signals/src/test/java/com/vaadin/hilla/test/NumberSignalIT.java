package com.vaadin.hilla.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import com.vaadin.testbench.parallel.Browser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WindowType;

@RunWith(BlockJUnit4ClassRunner.class)
public class NumberSignalIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();
        getDriver().get(getRootURL() + "/SharedNumberSignal");
        waitForPageToLoad();
    }

    private void waitForPageToLoad() {
        waitForElementPresent(By.ById.id("sharedValue"));
        waitForElementPresent(By.ById.id("counter"));
        waitUntil(driver -> $("span").id("sharedValue").getText() != null);
        waitUntil(driver -> $("span").id("counter").getText() != null);
        // Make sure the initial signal subscription round trip
        // completes (on CI):
        waitForMillis(1000);
    }

    @Test
    public void shouldUpdateValue_both_on_browser_and_server() {
        for (int i = 0; i < 5; i++) {
            var currentSharedValue = getSharedValue();
            clickButton("increaseSharedValue");
            Assert.assertEquals(currentSharedValue + 2, getSharedValue(), 0.0);
            Assert.assertEquals(getSharedValue(), fetchSharedValue(), 0.0);
        }

        for (int i = 0; i < 5; i++) {
            var currentCounterValue = getCounterValue();
            clickButton("incrementCounter");
            Assert.assertEquals(currentCounterValue + 1, getCounterValue());
            Assert.assertEquals(getCounterValue(), fetchCounterValue());
        }
    }

    @Test
    public void shouldUpdateValue_forOtherClients() {
        var currentSharedValue = getSharedValue();
        var currentCounterValue = getCounterValue();
        var firstWindowHandle = getDriver().getWindowHandle();

        var secondWindowDriver = getDriver().switchTo()
                .newWindow(WindowType.WINDOW);
        try {
            var secondWindowHandle = secondWindowDriver.getWindowHandle();

            Assert.assertNotEquals(firstWindowHandle, secondWindowHandle);

            secondWindowDriver.get(getRootURL() + "/SharedNumberSignal");

            var secondWindowSharedValue = Double.parseDouble(secondWindowDriver
                    .findElement(By.id("sharedValue")).getText());
            Assert.assertEquals(currentSharedValue, secondWindowSharedValue,
                    0.0);

            var secondWindowCounterValue = Long.parseLong(
                    secondWindowDriver.findElement(By.id("counter")).getText());
            Assert.assertEquals(currentCounterValue, secondWindowCounterValue);

            // press reset button on the second window
            secondWindowDriver.findElement(By.id("reset")).click();
            waitForMillis(500);

            secondWindowSharedValue = Double.parseDouble(secondWindowDriver
                    .findElement(By.id("sharedValue")).getText());
            Assert.assertEquals(0.5, secondWindowSharedValue, 0.0);

            secondWindowCounterValue = Long.parseLong(
                    secondWindowDriver.findElement(By.id("counter")).getText());
            Assert.assertEquals(0, secondWindowCounterValue);

            // check that the first window is also updated:
            getDriver().switchTo().window(firstWindowHandle);
            waitForMillis(500);
            Assert.assertEquals(0.5, getSharedValue(), 0.0);
            Assert.assertEquals(0, getCounterValue());

        } finally {
            secondWindowDriver.close();
        }
    }

    private double getSharedValue() {
        return Double.parseDouble($("span").id("sharedValue").getText());
    }

    private long getCounterValue() {
        return Long.parseLong($("span").id("counter").getText());
    }

    private double fetchSharedValue() {
        clickButton("fetchSharedValue");
        return Double
                .parseDouble($("span").id("sharedValueFromServer").getText());
    }

    private long fetchCounterValue() {
        clickButton("fetchCounterValue");
        return Long.parseLong($("span").id("counterValueFromServer").getText());
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }

    private void waitForMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
