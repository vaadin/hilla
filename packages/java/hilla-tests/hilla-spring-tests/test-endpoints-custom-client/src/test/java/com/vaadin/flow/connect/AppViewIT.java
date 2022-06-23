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

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Class for testing issues in a spring-boot container.
 */
public class AppViewIT extends ChromeBrowserTest {

    private void openTestUrl(String url) {
        getDriver().get(getRootURL() + "/foo" + url);
    }

    private TestBenchElement mainView;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        openTestUrl("/");
        mainView = $("main-view").waitForFirst();
    }

    @Test
    public void should_requestAnonymously_connect_service() {
        TestBenchElement button = mainView.$(TestBenchElement.class)
                .id("helloAnonymous");
        button.click();

        // Wait for the server connect response
        waitUntil(ExpectedConditions.textToBePresentInElement(
                mainView.$(TestBenchElement.class).id("content"),
                "Hello, stranger!"), 25);

        // verify that the custom Connect client works
        waitUntil(
                ExpectedConditions.textMatches(By.id("log"), Pattern.compile(
                        "\\[LOG] AppEndpoint/helloAnonymous took \\d+ ms")),
                25);
    }

    @Test
    public void should_requestAnonymously_endpoint_wrapper() {
        TestBenchElement button = mainView.$(TestBenchElement.class)
                .id("helloAnonymousWrapper");
        button.click();

        // Wait for the server connect response
        waitUntil(ExpectedConditions.textToBePresentInElement(
                mainView.$(TestBenchElement.class).id("content"),
                "Hello, stranger!"), 25);

        // verify that the custom Connect client works
        waitUntil(
                ExpectedConditions.textMatches(By.id("log"), Pattern.compile(
                        "\\[LOG] AppEndpoint/helloAnonymous took \\d+ ms")),
                25);
    }

    @Test
    public void should_requestAnonymously_after_logout() throws Exception {
        openTestUrl("/logout");
        openTestUrl("/");

        mainView = $("main-view").waitForFirst();

        TestBenchElement button = mainView.$(TestBenchElement.class)
                .id("helloAnonymous");
        button.click();

        // Wait for the server connect response
        waitUntil(ExpectedConditions.textToBePresentInElement(
                mainView.$(TestBenchElement.class).id("content"),
                "Hello, stranger!"), 25);
    }
}
