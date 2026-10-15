/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.connect;

import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AppViewIT extends ChromeBrowserTest {

    @Override
    protected String getTestPath() {
        return "";
    }

    @Test
    public void endpointUsingJava17FeaturesWorks() {
        open();
        waitForElementPresent(By.id("name"));
        WebElement input = $("input").id("name");
        input.sendKeys("John");

        WebElement button = $("button").id("button");
        button.click();
        verifyResponse("Hi John!");

        input.clear();
        input.sendKeys("Jeff");
        button.click();
        verifyResponse("Hello Jeff");

        input.clear();
        input.sendKeys("Somebody");
        button.click();
        verifyResponse("Hello stranger!");
    }

    private void verifyResponse(String expected) {
        waitUntil(driver -> {
            return $("div").id("response").getText().equals(expected);
        });
    }

}
