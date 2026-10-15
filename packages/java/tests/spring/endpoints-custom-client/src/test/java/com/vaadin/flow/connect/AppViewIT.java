/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
