/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.example.application;

import java.time.Duration;

import org.junit.Before;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

/**
 * Base class for the ITs of the application compiled to a native image.
 */
public abstract class AbstractNativeIT extends ChromeBrowserTest {

    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    /**
     * The binary is started asynchronously by the exec plugin, so unlike the
     * other test applications there is nothing that waits for the server before
     * the tests run. The check of the base class, which is what fails first on
     * a server that is not up yet, is therefore retried here until the
     * application answers. It records its own success, so this happens once per
     * test run.
     */
    @Override
    @Before
    public void checkIfServerAvailable() {
        var deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();

        while (true) {
            try {
                super.checkIfServerAvailable();
                return;
            } catch (IllegalStateException e) {
                if (System.nanoTime() > deadline) {
                    throw new IllegalStateException(
                            "The application did not answer within "
                                    + STARTUP_TIMEOUT
                                    + ". Check the output of the binary.",
                            e);
                }
                sleep();
            }
        }
    }

    /**
     * Opens the given path of the application under test.
     *
     * @param path
     *            the path to open, starting with a slash
     */
    protected void open(String path) {
        getDriver().get(getRootURL() + path);
    }

    /**
     * Logs the given user in through the login view.
     *
     * @param username
     *            the user name, which is also the password of every test user
     */
    protected void login(String username) {
        open("/login");
        LoginFormElement login = $(LoginFormElement.class).waitForFirst();
        login.getUsernameField().setValue(username);
        login.getPasswordField().setValue(username);
        login.submit();

        // The login view leaves itself once the endpoint has answered, and the
        // layout shows who is logged in
        waitUntil(driver -> !driver.getCurrentUrl().endsWith("/login"));
        waitUntil(driver -> username.equals($("*").id("user").getText()));
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for "
                    + "the application to answer", e);
        }
    }

}
