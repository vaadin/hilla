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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
     * the browser has landed on the page the login redirects to, away from the
     * login view.
     *
     * @param testComponent
     *            the test component that renders the login form
     * @param user
     *            the user to log in as
     */
    protected void login(TestBenchElement testComponent, String user) {
        testComponent.$(TestBenchElement.class).id("username").sendKeys(user);
        testComponent.$(TestBenchElement.class).id("password").sendKeys(user);
        WebElement submit = testComponent.$(TestBenchElement.class).id("login");
        submit.click();
        waitForNavigation(submit);

        // A rejected login lands back on the login view, which renders the
        // same test component as the page a successful one leads to. Without
        // this the test would carry on as if it were authenticated and fail
        // later on with an unexplained 401.
        waitUntil(driver -> !driver.getCurrentUrl().contains("/login"), 25);
    }

    /**
     * Waits until the navigation started in the document the given element
     * belongs to has replaced that document, and the document it navigated to
     * has finished loading.
     * <p>
     * Anything issued while a navigation is still in flight races with it - a
     * {@code getDriver().get(...)} in particular, which chromedriver may then
     * never report as complete, so the test hangs until the page load timeout
     * instead of failing fast. The element is the anchor for the wait because
     * it goes stale exactly when the new document commits; the readiness check
     * alone would be satisfied by the still-current document and pass before
     * the navigation had even started.
     *
     * @param elementOfPreviousDocument
     *            an element of the document that started the navigation
     */
    protected void waitForNavigation(WebElement elementOfPreviousDocument) {
        waitUntil(ExpectedConditions.stalenessOf(elementOfPreviousDocument),
                25);
        waitForDocumentReady();
    }

    /**
     * Waits until the document has finished loading and no reload is pending.
     */
    protected void waitForDocumentReady() {
        waitUntil(driver -> Boolean.TRUE
                .equals(getCommandExecutor().executeScript(
                        "return !window.reloadPending && window.document.readyState "
                                + "=== 'complete';")),
                25);
    }
}
