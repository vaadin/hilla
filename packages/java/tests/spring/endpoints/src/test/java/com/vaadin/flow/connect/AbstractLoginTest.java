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

import org.openqa.selenium.TimeoutException;
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
        //
        // The document the browser is on is the one the login led to by the
        // time this runs, so the view it is on is the answer of the server
        // rather than a question of time: what this says when it gives up is
        // where the browser ended up, since that is the only thing which
        // tells a rejected login from one the server never answered.
        try {
            waitUntil(driver -> !driver.getCurrentUrl().contains("/login"), 25);
        } catch (TimeoutException e) {
            throw new AssertionError("The login of " + user + " did not leave"
                    + " the login view, which is where a rejected login lands"
                    + " as well. The browser is at "
                    + getDriver().getCurrentUrl(), e);
        }
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
     * <p>
     * This is the wait which has to allow for an application which is still
     * warming up: the first login of a run is served while it is, and these
     * tests navigate with the driver itself rather than through the method
     * which waits for the development server.
     *
     * @param elementOfPreviousDocument
     *            an element of the document that started the navigation
     */
    protected void waitForNavigation(WebElement elementOfPreviousDocument) {
        waitUntil(ExpectedConditions.stalenessOf(elementOfPreviousDocument),
                60);
        waitForDocumentReady();
    }

    /**
     * Waits until the document has finished loading and no reload is pending,
     * which the document a login led to takes as long about as the application
     * serving it needs.
     */
    protected void waitForDocumentReady() {
        waitUntil(driver -> Boolean.TRUE
                .equals(getCommandExecutor().executeScript(
                        "return !window.reloadPending && window.document.readyState "
                                + "=== 'complete';")),
                60);
    }
}
