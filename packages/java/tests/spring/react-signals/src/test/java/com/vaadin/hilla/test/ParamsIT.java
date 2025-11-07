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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.parallel.Browser;

// Tests are disabled due to unstable signal implementation.
// Re-enable when there is a new signal implementation.
@Ignore
@RunWith(BlockJUnit4ClassRunner.class)
public class ParamsIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();
        getDriver().get(getRootURL() + "/ServiceMethodParams");
        waitForPageToLoad();
    }

    private void waitForPageToLoad() {
        waitForElementPresent(By.ById.id("valueH3"));
        waitUntil(driver -> $("h3").id("valueH3").getText() != null);
        waitForElementPresent(By.ById.id("valueSpan"));
        waitUntil(driver -> $("span").id("valueSpan").getText() != null);
    }

    @Test
    public void shouldObtainDifferentSignalInstances_when_callingServiceMethodWithDifferentParams() {
        long highValue = readValueFromPage();
        clickButton("increaseDecreaseBtn");
        waitFor(100);
        Assert.assertEquals(highValue + 1, readValueFromPage());

        // switch to the other signal instance:
        clickButton("toggleBtn");
        waitFor(500);

        long lowValue = readValueFromPage();
        clickButton("increaseDecreaseBtn");
        waitFor(100);
        Assert.assertEquals(lowValue - 1, readValueFromPage());

        lowValue = readValueFromPage();
        clickButton("increaseDecreaseBtn");
        waitFor(100);
        Assert.assertEquals(lowValue - 1, readValueFromPage());

        // switch back to the first signal instance:
        clickButton("toggleBtn");
        waitFor(500);

        // check that the first signal instance is still the same:
        Assert.assertEquals(highValue + 1, readValueFromPage());
    }

    private long readValueFromPage() {
        return Long.parseLong($("span").id("valueSpan").getText());
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
