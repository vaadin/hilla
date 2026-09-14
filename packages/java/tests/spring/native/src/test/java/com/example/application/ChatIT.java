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
import java.util.List;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.flow.component.login.testbench.LoginFormElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchDriverProxy;
import com.vaadin.testbench.TestBenchElement;

/**
 * The chat view subscribes to a <code>Flux</code> of an endpoint, so this
 * covers the push connection and the message types it serializes, all of which
 * need reflection in a native image.
 */
public class ChatIT extends AbstractNativeIT {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final String MESSAGE_FROM_USER_1 = "Hello from user 1";
    private static final String MESSAGE_FROM_USER_2 = "Goodbye from user 2";

    private TestBenchDriverProxy user1;
    private TestBenchDriverProxy user2;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        user1 = (TestBenchDriverProxy) getDriver();
        // A second browser, to see the messages of the other user
        super.setup();
        user2 = (TestBenchDriverProxy) getDriver();
    }

    @Test
    public void messagesReachBothUsers() {
        openChat(user1, "user1");
        openChat(user2, "user2");

        send(user1, MESSAGE_FROM_USER_1);
        waitForMessages(user1, List.of(MESSAGE_FROM_USER_1));
        waitForMessages(user2, List.of(MESSAGE_FROM_USER_1));

        send(user2, MESSAGE_FROM_USER_2);
        waitForMessages(user1,
                List.of(MESSAGE_FROM_USER_1, MESSAGE_FROM_USER_2));
        waitForMessages(user2,
                List.of(MESSAGE_FROM_USER_1, MESSAGE_FROM_USER_2));
    }

    @After
    public void quitTheFirstBrowser() {
        // The driver of the running test is quit by the base class
        if (user1 != null && user1 != getDriver()) {
            user1.quit();
        }
    }

    private void openChat(WebDriver driver, String username) {
        driver.get(getRootURL() + "/login");
        waitFor(driver, d -> !elements(d, "vaadin-login-form").isEmpty());

        LoginFormElement login = wrap(
                elements(driver, "vaadin-login-form").get(0),
                LoginFormElement.class);
        login.getUsernameField().setValue(username);
        login.getPasswordField().setValue(username);
        login.submit();
        // The chat view needs a logged in user, so the login has to have gone
        // through before it is opened
        waitFor(driver, d -> !d.getCurrentUrl().endsWith("/login"));

        driver.get(getRootURL() + "/chat");
        waitFor(driver, d -> !elements(d, "vaadin-message-input").isEmpty());
    }

    private void send(WebDriver driver, String message) {
        TestBenchElement input = elements(driver, "vaadin-message-input")
                .get(0);
        input.$(TextAreaElement.class).first().setValue(message);
        // The send button is a vaadin-message-input-button, so the plain
        // button query does not match it
        input.$("vaadin-message-input-button").first().click();
    }

    private void waitForMessages(WebDriver driver, List<String> expected) {
        waitFor(driver, d -> messagesOf(d).equals(expected));
    }

    private List<String> messagesOf(WebDriver driver) {
        return driver.findElement(By.tagName("vaadin-message-list"))
                .findElements(By.tagName("vaadin-message")).stream()
                .map(message -> ((TestBenchElement) message)
                        .getPropertyString("innerText"))
                .toList();
    }

    private List<TestBenchElement> elements(WebDriver driver, String tag) {
        return driver.findElements(By.tagName(tag)).stream()
                .map(TestBenchElement.class::cast).toList();
    }

    private <T extends TestBenchElement> T wrap(TestBenchElement element,
            Class<T> type) {
        return TestBench.wrap(element, type);
    }

    private void waitFor(WebDriver driver, Predicate<WebDriver> condition) {
        new WebDriverWait(driver, TIMEOUT).until(condition::test);
    }

}
