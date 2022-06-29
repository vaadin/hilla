/*
 * Copyright 2000-2017 Vaadin Ltd.
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
