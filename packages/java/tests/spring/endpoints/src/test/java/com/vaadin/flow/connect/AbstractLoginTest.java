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
package com.vaadin.flow.connect;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Base class for the ITs that authenticate through the login form rendered by
 * one of the test components.
 */
public abstract class AbstractLoginTest extends ChromeBrowserTest {

    /**
     * Fills in and submits the login form of the given test component, using
     * the given user as both the user name and the password, and waits until
     * the browser has landed on the page the login redirects to.
     *
     * @param testComponent
     *            the test component that renders the login form
     * @param user
     *            the user to log in as
     */
    protected void login(TestBenchElement testComponent, String user) {
        testComponent.$(TestBenchElement.class).id("username").sendKeys(user);
        testComponent.$(TestBenchElement.class).id("password").sendKeys(user);
        testComponent.$(TestBenchElement.class).id("login").click();
        waitForLoginRedirect();
    }

    /**
     * Waits until the login form submission and the Spring Security redirect
     * away from the login view have both completed.
     * <p>
     * Submitting the form navigates the browser, and so does the redirect that
     * follows it. Anything issued while either navigation is still in flight
     * races with it - a {@code getDriver().get(...)} in particular, which
     * chromedriver may then never report as complete, so the test hangs until
     * the page load timeout instead of failing fast.
     */
    protected void waitForLoginRedirect() {
        // Wait for the form submission to complete and page to redirect
        waitForDocumentReady();

        // Wait for Spring Security redirect to complete (URL should no longer
        // contain /login)
        waitUntil(driver -> !driver.getCurrentUrl().contains("/login"));
    }

    /**
     * Waits until the document has finished loading and no reload is pending.
     */
    protected void waitForDocumentReady() {
        waitUntil(driver -> Boolean.TRUE
                .equals(getCommandExecutor().executeScript(
                        "return !window.reloadPending && window.document.readyState "
                                + "=== 'complete';")));
    }
}
