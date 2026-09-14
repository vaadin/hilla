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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;

/**
 * The menu is built from the views the file router found, which the client
 * reads from <code>file-routes.json</code>. That file, and the route metadata
 * types behind it, only reach the browser when they are registered as resources
 * and for reflection in the native image.
 */
public class MenuIT extends AbstractNativeIT {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open("/");
    }

    @Test
    public void menuListsTheViewsAnAnonymousUserMayOpen() {
        // The chat view requires a login, so it is left out here.
        Assert.assertEquals(
                List.of("Endpoint access", "Form", "Grid", "Home", "I18n"),
                menuTitles());
    }

    @Test
    public void menuListsTheViewThatNeedsALoginAfterLoggingIn() {
        login("user1");
        open("/");

        Assert.assertEquals(List.of("Chat", "Endpoint access", "Form", "Grid",
                "Home", "I18n"), menuTitles());
    }

    @Test
    public void theViewOfAMenuItemOpens() {
        // The link of the item is in its shadow root
        menuItem("Grid").$("a").first().click();

        waitUntil(driver -> !$("vaadin-grid").all().isEmpty());
    }

    private List<String> menuTitles() {
        waitUntil(driver -> !menuItems().isEmpty());
        return menuItems().stream().map(MenuIT::titleOf).sorted().toList();
    }

    private TestBenchElement menuItem(String title) {
        waitUntil(driver -> !menuItems().isEmpty());
        return menuItems().stream().filter(item -> title.equals(titleOf(item)))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "No menu item " + title + " in " + menuTitles()));
    }

    /**
     * The title is in the light DOM of the item, which getText() does not
     * report for a side nav item.
     */
    private static String titleOf(TestBenchElement item) {
        return item.getPropertyString("textContent").trim();
    }

    private List<TestBenchElement> menuItems() {
        return $("vaadin-side-nav-item").all();
    }

}
