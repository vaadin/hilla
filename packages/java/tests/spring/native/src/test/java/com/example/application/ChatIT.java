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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.testbench.TestBenchElement;

/**
 * The chat view subscribes to a <code>Flux</code> of an endpoint, so this
 * covers the push connection and the message types it serializes, all of which
 * need reflection in a native image.
 */
public class ChatIT extends AbstractNativeIT {

    private static final String MESSAGE_FROM_USER_1 = "Hello from user 1";
    private static final String MESSAGE_FROM_USER_2 = "Goodbye from user 2";

    private WebDriver user1;
    private WebDriver user2;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        user1 = getDriver();
        // A second browser, to see the messages of the other user
        super.setup();
        user2 = getDriver();
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
    public void quitTheBrowserTheTestDidNotEndWith() {
        if (user1 != null && user1 != getDriver()) {
            user1.quit();
        }
        if (user2 != null && user2 != getDriver()) {
            user2.quit();
        }
    }

    /**
     * Points the test at the browser of the given user, so that the helpers of
     * the base class work on it.
     */
    private void as(WebDriver user) {
        setDriver(user);
    }

    private void openChat(WebDriver user, String username) {
        as(user);
        login(username);
        open("/chat");
        waitUntil(driver -> !$("vaadin-message-input").all().isEmpty());
    }

    private void send(WebDriver user, String message) {
        as(user);
        TestBenchElement input = $("vaadin-message-input").first();
        input.$(TextAreaElement.class).first().setValue(message);
        // The send button is a vaadin-message-input-button, so the plain
        // button query does not match it
        input.$("vaadin-message-input-button").first().click();
    }

    private void waitForMessages(WebDriver user, List<String> expected) {
        as(user);
        waitUntil(driver -> messages().equals(expected));
    }

    private List<String> messages() {
        return getDriver().findElements(By.tagName("vaadin-message")).stream()
                .map(message -> ((TestBenchElement) message)
                        .getPropertyString("innerText"))
                .toList();
    }

}
