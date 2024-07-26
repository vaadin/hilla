package com.vaadin.hilla.test;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class NumberSignalIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/number-signal");
        waitForPageToLoad();
    }

    private void waitForPageToLoad() {
        waitForElementPresent(By.ById.id("sharedValue"));
        waitForElementPresent(By.ById.id("counter"));
    }

    @Test
    public void shouldHaveDefaultValueLoadedFromServer() {
        Assert.assertEquals("Shared value: 0.5",
                $("span").id("sharedValue").getText());
        Assert.assertEquals("Counter value: 0",
                $("span").id("counter").getText());
    }
}
