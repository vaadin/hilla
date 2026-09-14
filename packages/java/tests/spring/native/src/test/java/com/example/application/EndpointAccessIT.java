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

import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;

/**
 * The access checks of an endpoint read its annotations reflectively. Without
 * the hints for the endpoint classes a native image lets nothing through, so
 * both the calls that must be allowed and the ones that must be denied are
 * covered here.
 */
public class EndpointAccessIT extends AbstractNativeIT {

    @Test
    public void anonymousUserMayCallTheAnonymousMethodOnly() {
        assertResult("anonymous", "anyone");
        assertResult("authenticated", "denied");
        assertResult("admin", "denied");
    }

    @Test
    public void userMayNotCallTheAdminMethod() {
        login("user1");

        assertResult("authenticated", "any user");
        assertResult("admin", "denied");
    }

    @Test
    public void adminMayCallEveryMethod() {
        login("admin");

        assertResult("anonymous", "anyone");
        assertResult("authenticated", "any user");
        assertResult("admin", "admin");
    }

    /**
     * Opens the view before every call, because a view that has just been
     * opened shows no result yet: whatever is read afterwards is the answer to
     * this call, and not the one a call before it happened to leave behind.
     */
    private void assertResult(String button, String expected) {
        open("/endpoint-access");
        waitUntil(driver -> !$(ButtonElement.class).withAttribute("id", button)
                .all().isEmpty());

        $(ButtonElement.class).id(button).click();
        waitUntil(driver -> expected.equals($("*").id("result").getText()));
    }

}
